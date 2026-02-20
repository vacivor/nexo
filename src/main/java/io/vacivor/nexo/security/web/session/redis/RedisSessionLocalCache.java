package io.vacivor.nexo.security.web.session.redis;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.vacivor.nexo.security.core.session.RedisSessionSettings;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class RedisSessionLocalCache {

  private final RedisSessionSettings configuration;
  private final Cache<String, RedisSession> cache;

  public RedisSessionLocalCache(RedisSessionSettings configuration) {
    this.configuration = configuration;
    this.cache = Caffeine.newBuilder()
        .maximumSize(Math.max(1, configuration.getRedisLocalCacheMaximumSize()))
        .expireAfterWrite(configuration.getRedisLocalCacheTtl())
        .build();
  }

  public Optional<RedisSession> get(String sessionId) {
    if (!configuration.isRedisLocalCacheEnabled()) {
      return Optional.empty();
    }
    RedisSession session = cache.getIfPresent(sessionId);
    return Optional.ofNullable(session);
  }

  public void put(RedisSession session) {
    if (!configuration.isRedisLocalCacheEnabled() || session == null) {
      return;
    }
    cache.put(session.getId(), session);
  }

  public void invalidate(String sessionId) {
    if (sessionId == null || sessionId.isBlank()) {
      return;
    }
    cache.invalidate(sessionId);
  }
}
