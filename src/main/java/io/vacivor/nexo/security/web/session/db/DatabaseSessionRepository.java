package io.vacivor.nexo.security.web.session.db;

import io.micronaut.context.annotation.Requires;
import io.vacivor.nexo.security.web.session.Session;
import io.vacivor.nexo.security.web.session.SessionAttributesCodec;
import io.vacivor.nexo.security.web.session.SessionConfiguration;
import io.vacivor.nexo.security.web.session.SessionRepository;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Singleton
@Requires(property = "nexo.session.store", value = "db")
public class DatabaseSessionRepository implements SessionRepository {

  private final SessionEntityRepository entityRepository;
  private final SessionConfiguration configuration;
  private final SessionAttributesCodec attributesCodec;

  public DatabaseSessionRepository(SessionEntityRepository entityRepository,
      SessionConfiguration configuration,
      SessionAttributesCodec attributesCodec) {
    this.entityRepository = entityRepository;
    this.configuration = configuration;
    this.attributesCodec = attributesCodec;
  }

  @Override
  public Session createSession(String id) {
    DatabaseSession session = new DatabaseSession(id);
    session.setMaxInactiveInterval(configuration.getMaxInactiveInterval());
    return session;
  }

  @Override
  public Optional<Session> findById(String id) {
    return entityRepository.findById(id)
        .map(entity -> (Session) toSession(entity))
        .filter(session -> {
          if (session.isExpired()) {
            deleteById(id);
            return false;
          }
          return true;
        });
  }

  @Override
  public Session save(Session session) {
    if (!(session instanceof DatabaseSession)) {
      throw new IllegalArgumentException("DatabaseSessionRepository only supports DatabaseSession");
    }
    DatabaseSession dbSession = (DatabaseSession) session;
    entityRepository.save(toEntity(dbSession));
    return dbSession;
  }

  @Override
  public void deleteById(String id) {
    entityRepository.deleteById(id);
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
