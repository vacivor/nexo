package io.vacivor.nexo.security.web.session.db;

import io.micronaut.context.annotation.Requires;
import io.vacivor.nexo.security.web.session.SessionAttributesCodec;
import io.vacivor.nexo.security.core.session.SessionCursor;
import io.vacivor.nexo.security.core.session.SessionSettings;
import io.vacivor.nexo.security.core.session.SessionRepository;
import io.vacivor.nexo.dal.entity.SessionEntity;
import io.vacivor.nexo.dal.repository.SessionEntityRepository;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
@Requires(property = "nexo.security.session.store", value = "db")
public class DatabaseSessionRepository implements SessionRepository<DatabaseSession> {

  private final SessionEntityRepository entityRepository;
  private final SessionSettings configuration;
  private final SessionAttributesCodec attributesCodec;

  public DatabaseSessionRepository(SessionEntityRepository entityRepository,
      SessionSettings configuration,
      SessionAttributesCodec attributesCodec) {
    this.entityRepository = entityRepository;
    this.configuration = configuration;
    this.attributesCodec = attributesCodec;
  }

  @Override
  public DatabaseSession createSession(String id) {
    DatabaseSession session = new DatabaseSession(id);
    session.setMaxInactiveInterval(configuration.getMaxInactiveInterval());
    return session;
  }

  @Override
  public Optional<DatabaseSession> findById(String id) {
    return entityRepository.findById(id)
        .map(this::toSession)
        .filter(session -> {
          if (session.isExpired()) {
            deleteById(id);
            return false;
          }
          return true;
        });
  }

  @Override
  public DatabaseSession save(DatabaseSession session) {
    entityRepository.save(toEntity(session));
    return session;
  }

  @Override
  public void deleteById(String id) {
    entityRepository.deleteById(id);
  }

  @Override
  public List<DatabaseSession> findAllSessions() {
    List<DatabaseSession> all = new ArrayList<>();
    for (SessionEntity entity : entityRepository.findAll()) {
      DatabaseSession session = toSession(entity);
      if (session.isExpired()) {
        deleteById(session.getId());
        continue;
      }
      all.add(session);
    }
    all.sort(Comparator.comparing(DatabaseSession::getLastAccessedTime).reversed());
    return all;
  }

  @Override
  public List<DatabaseSession> findSessions(int offset, int limit) {
    if (limit <= 0) {
      return List.of();
    }
    int normalizedOffset = Math.max(0, offset);
    List<DatabaseSession> page = new ArrayList<>();
    for (SessionEntity entity : entityRepository.findPage(normalizedOffset, limit)) {
      DatabaseSession session = toSession(entity);
      if (session.isExpired()) {
        deleteById(session.getId());
        continue;
      }
      page.add(session);
    }
    return page;
  }

  @Override
  public List<DatabaseSession> findSessionsByCursor(String cursor, int limit) {
    if (limit <= 0) {
      return List.of();
    }
    Optional<SessionCursor.CursorValue> cursorValue = SessionCursor.decode(cursor);
    List<SessionEntity> entities;
    if (cursorValue.isPresent()) {
      SessionCursor.CursorValue value = cursorValue.get();
      entities = entityRepository.findPageByCursor(value.lastAccessedAt(), value.id(), limit);
    } else {
      entities = entityRepository.findFirstPage(limit);
    }
    List<DatabaseSession> page = new ArrayList<>();
    for (SessionEntity entity : entities) {
      DatabaseSession session = toSession(entity);
      if (session.isExpired()) {
        deleteById(session.getId());
        continue;
      }
      page.add(session);
    }
    return page;
  }

  @Override
  public long countSessions() {
    return entityRepository.countAllSessions();
  }

  private DatabaseSession toSession(SessionEntity entity) {
    Duration maxInactive = entity.getMaxInactiveIntervalSeconds() == null
        ? null
        : Duration.ofSeconds(entity.getMaxInactiveIntervalSeconds());
    Map<String, Object> attributes = attributesCodec.decode(entity.getAttributesJson());
    return new DatabaseSession(entity.getId(), entity.getCreationTime(),
        entity.getLastAccessedTime(), maxInactive, Boolean.TRUE.equals(entity.getNew()), attributes);
  }

  private SessionEntity toEntity(DatabaseSession session) {
    SessionEntity entity = new SessionEntity();
    entity.setId(session.getId());
    entity.setCreationTime(session.getCreationTime());
    entity.setLastAccessedTime(session.getLastAccessedTime());
    entity.setNew(session.isNew());
    entity.setAttributesJson(attributesCodec.encode(session.asMap()));
    session.getMaxInactiveInterval().ifPresent(duration ->
        entity.setMaxInactiveIntervalSeconds(duration.getSeconds()));
    return entity;
  }
}
