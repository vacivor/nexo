package io.vacivor.nexo.security.web.session.inmemory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import io.micronaut.context.annotation.Requires;
import io.vacivor.nexo.security.core.session.SessionSettings;
import io.vacivor.nexo.security.core.session.SessionRepository;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
@Requires(property = "nexo.security.session.store", value = "inmemory")
public class InMemorySessionRepository implements SessionRepository<InMemorySession> {

  private final SessionSettings configuration;
  private final Cache<String, InMemorySession> cache;

  public InMemorySessionRepository(SessionSettings configuration) {
    this.configuration = configuration;
    this.cache = Caffeine.newBuilder()
        .maximumSize(configuration.getInMemoryMaximumSize())
        .expireAfter(new SessionExpiry())
        .build();
  }

  @Override
  public InMemorySession createSession(String id) {
    InMemorySession session = new InMemorySession(id);
    session.setMaxInactiveInterval(configuration.getMaxInactiveInterval());
    return session;
  }

  @Override
  public Optional<InMemorySession> findById(String id) {
    InMemorySession session = cache.getIfPresent(id);
    if (session == null) {
      return Optional.empty();
    }
    if (session.isExpired()) {
      cache.invalidate(id);
      return Optional.empty();
    }
    return Optional.of(session);
  }

  @Override
  public InMemorySession save(InMemorySession session) {
    cache.put(session.getId(), session);
    return session;
  }

  @Override
  public void deleteById(String id) {
    cache.invalidate(id);
  }

  @Override
  public List<InMemorySession> findAllSessions() {
    return activeSessions();
  }

  @Override
  public List<InMemorySession> findSessions(int offset, int limit) {
    if (limit <= 0) {
      return List.of();
    }
    int normalizedOffset = Math.max(0, offset);
    int skipped = 0;
    List<InMemorySession> page = new ArrayList<>(limit);
    for (InMemorySession session : cache.asMap().values()) {
      if (session.isExpired()) {
        cache.invalidate(session.getId());
        continue;
      }
      if (skipped < normalizedOffset) {
        skipped++;
        continue;
      }
      page.add(session);
      if (page.size() >= limit) {
        break;
      }
    }
    return page;
  }

  @Override
  public long countSessions() {
    return activeSessions().size();
  }

  private List<InMemorySession> activeSessions() {
    List<InMemorySession> all = new ArrayList<>();
    for (InMemorySession session : cache.asMap().values()) {
      if (session.isExpired()) {
        cache.invalidate(session.getId());
        continue;
      }
      all.add(session);
    }
    return all;
  }

  private static class SessionExpiry implements Expiry<String, InMemorySession> {

    @Override
    public long expireAfterCreate(String key, InMemorySession value, long currentTime) {
      return toNanos(value.getMaxInactiveInterval().orElse(null));
    }

    @Override
    public long expireAfterUpdate(String key, InMemorySession value, long currentTime,
        long currentDuration) {
      return toNanos(value.getMaxInactiveInterval().orElse(null));
    }

    @Override
    public long expireAfterRead(String key, InMemorySession value, long currentTime,
        long currentDuration) {
      return toNanos(value.getMaxInactiveInterval().orElse(null));
    }

    private long toNanos(Duration duration) {
      if (duration == null) {
        return Long.MAX_VALUE;
      }
      if (duration.isZero() || duration.isNegative()) {
        return 0L;
      }
      return duration.toNanos();
    }
  }
}
