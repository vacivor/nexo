package io.vacivor.nexo.security.web.session.map;

import io.micronaut.context.annotation.Requires;
import io.vacivor.nexo.security.web.session.SessionConfiguration;
import io.vacivor.nexo.security.web.session.SessionRepository;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@Requires(property = "nexo.session.store", value = "map")
public class MapSessionRepository implements SessionRepository<MapSession> {

  private final Map<String, MapSession> sessions = new ConcurrentHashMap<>();
  private final SessionConfiguration configuration;

  public MapSessionRepository(SessionConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public MapSession createSession(String id) {
    MapSession session = new MapSession(id);
    session.setMaxInactiveInterval(configuration.getMaxInactiveInterval());
    return session;
  }

  @Override
  public Optional<MapSession> findById(String id) {
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
  public MapSession save(MapSession session) {
    sessions.put(session.getId(), session);
    return session;
  }

  @Override
  public void deleteById(String id) {
    sessions.remove(id);
  }
}
