package io.vacivor.nexo.security.web.session.redis;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.micronaut.context.annotation.Requires;
import io.vacivor.nexo.security.web.session.Session;
import io.vacivor.nexo.security.web.session.SessionAttributesCodec;
import io.vacivor.nexo.security.web.session.SessionConfiguration;
import io.vacivor.nexo.security.web.session.SessionRepository;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
@Requires(property = "nexo.session.store", value = "redis")
public class RedisSessionRepository implements SessionRepository {

  private static final String FIELD_CREATION_TIME = "creationTime";
  private static final String FIELD_LAST_ACCESSED_TIME = "lastAccessedTime";
  private static final String FIELD_MAX_INACTIVE_INTERVAL = "maxInactiveIntervalSeconds";
  private static final String FIELD_IS_NEW = "isNew";
  private static final String FIELD_ATTRIBUTES = "attributes";

  private final RedisCommands<String, String> commands;
  private final SessionConfiguration configuration;
  private final SessionAttributesCodec attributesCodec;

  public RedisSessionRepository(StatefulRedisConnection<String, String> connection,
      SessionConfiguration configuration,
      SessionAttributesCodec attributesCodec) {
    this.commands = connection.sync();
    this.configuration = configuration;
    this.attributesCodec = attributesCodec;
  }

  @Override
  public Session createSession(String id) {
    RedisSession session = new RedisSession(id);
    session.setMaxInactiveInterval(configuration.getMaxInactiveInterval());
    return session;
  }

  @Override
  public Optional<Session> findById(String id) {
    Map<String, String> data = commands.hgetall(key(id));
    if (data == null || data.isEmpty()) {
      return Optional.empty();
    }
    RedisSession session = fromHash(id, data);
    if (session.isExpired()) {
      deleteById(id);
      return Optional.empty();
    }
    return Optional.of(session);
  }

  @Override
  public Session save(Session session) {
    if (!(session instanceof RedisSession)) {
      throw new IllegalArgumentException("RedisSessionRepository only supports RedisSession");
    }
    RedisSession redisSession = (RedisSession) session;
    commands.hset(key(redisSession.getId()), toHash(redisSession));
    long ttlSeconds = toTtlSeconds(redisSession);
    if (ttlSeconds > 0) {
      commands.expire(key(redisSession.getId()), ttlSeconds);
    }
    return redisSession;
  }

  @Override
  public void deleteById(String id) {
    commands.del(key(id));
  }

  private String key(String id) {
    return configuration.getRedisKeyPrefix() + id;
  }

  private long toTtlSeconds(RedisSession session) {
    return session.getMaxInactiveInterval()
        .map(Duration::getSeconds)
        .orElse(0L);
  }

  private Map<String, String> toHash(RedisSession session) {
    Map<String, String> map = new HashMap<>();
    map.put(FIELD_CREATION_TIME, String.valueOf(session.getCreationTime().toEpochMilli()));
    map.put(FIELD_LAST_ACCESSED_TIME, String.valueOf(session.getLastAccessedTime().toEpochMilli()));
    map.put(FIELD_IS_NEW, String.valueOf(session.isNew()));
    session.getMaxInactiveInterval().ifPresent(duration ->
        map.put(FIELD_MAX_INACTIVE_INTERVAL, String.valueOf(duration.getSeconds())));
    map.put(FIELD_ATTRIBUTES, attributesCodec.encode(session.asMap()));
    return map;
  }

  private RedisSession fromHash(String id, Map<String, String> data) {
    Instant creationTime = Instant.ofEpochMilli(Long.parseLong(data.get(FIELD_CREATION_TIME)));
    Instant lastAccessed = Instant.ofEpochMilli(Long.parseLong(data.get(FIELD_LAST_ACCESSED_TIME)));
    Duration maxInactive = null;
    String maxInactiveValue = data.get(FIELD_MAX_INACTIVE_INTERVAL);
    if (maxInactiveValue != null) {
      maxInactive = Duration.ofSeconds(Long.parseLong(maxInactiveValue));
    }
    boolean isNew = Boolean.parseBoolean(data.getOrDefault(FIELD_IS_NEW, "false"));
    Map<String, Object> attributes = attributesCodec.decode(data.get(FIELD_ATTRIBUTES));
    return new RedisSession(id, creationTime, lastAccessed, maxInactive, isNew, attributes);
  }
}
