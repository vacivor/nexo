package io.vacivor.nexo.security.web.session.map;

import io.micronaut.context.annotation.Requires;
import io.vacivor.nexo.security.web.session.Session;
import io.vacivor.nexo.security.web.session.SessionConfiguration;
import io.vacivor.nexo.security.web.session.SessionRepository;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@Requires(property = "nexo.session.store", value = "map", defaultValue = "map")
public class MapSessionRepository implements SessionRepository {

  private final Map<String, MapSession> sessions = new ConcurrentHashMap<>();
  private final SessionConfiguration configuration;

  public MapSessionRepository(SessionConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public Session createSession(String id) {
    MapSession session = new MapSession(id);
    session.setMaxInactiveInterval(configuration.getMaxInactiveInterval());
    return session;
  }

  @Override
  public Optional<Session> findById(String id) {
    MapSession session = sessions.get(id);
    if (session == null) {
      return Optional.empty();
    }
    if (session.isExpired()) {
      sessions.remove(id);
      return Optional.empty();
    }
    return Optional.of(session);
  }

  @Override
  public Session save(Session session) {
    if (!(session instanceof MapSession)) {
      throw new IllegalArgumentException("MapSessionRepository only supports MapSession");
    }
    MapSession mapSession = (MapSession) session;
    sessions.put(mapSession.getId(), mapSession);
    return mapSession;
  }

  @Override
  public void deleteById(String id) {
    sessions.remove(id);
  }
}
