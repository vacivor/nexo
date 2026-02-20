package io.vacivor.nexo.security.web.session.redis;

import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.micronaut.context.annotation.Requires;
import io.vacivor.nexo.security.web.session.SessionAttributesCodec;
import io.vacivor.nexo.security.web.session.SessionConfiguration;
import io.vacivor.nexo.security.web.session.SessionRepository;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Singleton
@Requires(property = "nexo.security.session.store", value = "redis")
@Requires(beans = StatefulRedisClusterConnection.class)
public class RedisClusterSessionRepository implements SessionRepository<RedisSession> {

  private static final String FIELD_CREATION_TIME = "creationTime";
  private static final String FIELD_LAST_ACCESSED_TIME = "lastAccessedTime";
  private static final String FIELD_MAX_INACTIVE_INTERVAL = "maxInactiveIntervalSeconds";
  private static final String FIELD_IS_NEW = "isNew";
  private static final String FIELD_ATTRIBUTES = "attributes";
  private static final String FIELD_EXPIRATION_BUCKET_EPOCH_MINUTE = "expirationBucketEpochMinute";
  private static final Duration SESSION_GRACE_PERIOD = Duration.ofMinutes(5);

  private final RedisAdvancedClusterCommands<String, String> commands;
  private final SessionConfiguration configuration;
  private final SessionAttributesCodec attributesCodec;
  private final RedisSessionLocalCache localCache;

  public RedisClusterSessionRepository(StatefulRedisClusterConnection<String, String> connection,
      SessionConfiguration configuration,
      SessionAttributesCodec attributesCodec,
      RedisSessionLocalCache localCache) {
    this.commands = connection.sync();
    this.configuration = configuration;
    this.attributesCodec = attributesCodec;
    this.localCache = localCache;
  }

  @Override
  public RedisSession createSession(String id) {
    RedisSession session = new RedisSession(id);
    session.setMaxInactiveInterval(configuration.getMaxInactiveInterval());
    return session;
  }

  @Override
  public Optional<RedisSession> findById(String id) {
    Optional<RedisSession> cached = localCache.get(id);
    if (cached.isPresent()) {
      RedisSession session = cached.get();
      if (session.isExpired()) {
        localCache.invalidate(id);
        deleteById(id);
        return Optional.empty();
      }
      return Optional.of(session);
    }
    Map<String, String> data = commands.hgetall(key(id));
    if (data == null || data.isEmpty()) {
      return Optional.empty();
    }
    RedisSession session = fromHash(id, data);
    if (session.isExpired()) {
      deleteById(id);
      return Optional.empty();
    }
    localCache.put(session);
    return Optional.of(session);
  }

  @Override
  public RedisSession save(RedisSession session) {
    String sessionKey = key(session.getId());
    String expiresKey = expiresKey(session.getId());
    Long previousBucketEpochMinute = readBucketEpochMinute(sessionKey);
    boolean created = commands.exists(sessionKey) != null && commands.exists(sessionKey) == 0;
    long ttlSeconds = toTtlSeconds(session);
    Long nextBucketEpochMinute = ttlSeconds > 0 ? toBucketEpochMinute(session, ttlSeconds) : null;
    commands.hset(sessionKey, toHash(session, nextBucketEpochMinute));
    if (ttlSeconds > 0) {
      commands.expire(sessionKey, ttlSeconds + SESSION_GRACE_PERIOD.getSeconds());
      commands.set(expiresKey, "", io.lettuce.core.SetArgs.Builder.ex(ttlSeconds));
      if (nextBucketEpochMinute != null) {
        String bucketKey = expirationsKey(nextBucketEpochMinute);
        commands.sadd(bucketKey, expiresKey);
        commands.expire(bucketKey, ttlSeconds + SESSION_GRACE_PERIOD.getSeconds());
      }
    } else {
      commands.persist(sessionKey);
      commands.del(expiresKey);
    }
    if (previousBucketEpochMinute != null
        && !previousBucketEpochMinute.equals(nextBucketEpochMinute)) {
      commands.srem(expirationsKey(previousBucketEpochMinute), expiresKey);
    }
    if (created) {
      commands.publish(createdChannel(session.getId()), session.getId());
    }
    localCache.put(session);
    return session;
  }

  @Override
  public void deleteById(String id) {
    String sessionKey = key(id);
    Long bucketEpochMinute = readBucketEpochMinute(sessionKey);
    commands.del(sessionKey);
    String expiresKey = expiresKey(id);
    commands.del(expiresKey);
    if (bucketEpochMinute != null) {
      commands.srem(expirationsKey(bucketEpochMinute), expiresKey);
    }
    localCache.invalidate(id);
  }

  @Override
  public List<RedisSession> findAllSessions() {
    return findSessions(0, Integer.MAX_VALUE);
  }

  @Override
  public List<RedisSession> findSessions(int offset, int limit) {
    if (limit <= 0) {
      return List.of();
    }
    int normalizedOffset = Math.max(0, offset);
    int seen = 0;
    List<RedisSession> page = new ArrayList<>(limit);
    Set<String> visitedIds = new HashSet<>();
    String prefix = sessionKeyPrefix();
    ScanArgs scanArgs = ScanArgs.Builder.matches(prefix + "*").limit(Math.max(100, limit * 4));
    ScanCursor cursor = ScanCursor.INITIAL;
    while (true) {
      KeyScanCursor<String> keyScan = commands.scan(cursor, scanArgs);
      List<String> keys = keyScan.getKeys();
      if (keys == null || keys.isEmpty()) {
        if (keyScan.isFinished()) {
          break;
        }
        cursor = keyScan;
        continue;
      }
      for (String key : keys) {
        if (key == null || !key.startsWith(prefix)) {
          continue;
        }
        String id = key.substring(prefix.length());
        if (!visitedIds.add(id)) {
          continue;
        }
        Optional<RedisSession> sessionOpt = findById(id);
        if (sessionOpt.isEmpty()) {
          continue;
        }
        if (seen < normalizedOffset) {
          seen++;
          continue;
        }
        page.add(sessionOpt.get());
        if (page.size() >= limit) {
          return page;
        }
      }
      if (keyScan.isFinished()) {
        break;
      }
      cursor = keyScan;
    }
    return page;
  }

  @Override
  public long countSessions() {
    String prefix = sessionKeyPrefix();
    ScanArgs scanArgs = ScanArgs.Builder.matches(prefix + "*").limit(500);
    ScanCursor cursor = ScanCursor.INITIAL;
    long total = 0;
    Set<String> visitedIds = new HashSet<>();
    while (true) {
      KeyScanCursor<String> keyScan = commands.scan(cursor, scanArgs);
      List<String> keys = keyScan.getKeys();
      if (keys == null || keys.isEmpty()) {
        if (keyScan.isFinished()) {
          break;
        }
        cursor = keyScan;
        continue;
      }
      for (String key : keys) {
        if (key == null || !key.startsWith(prefix)) {
          continue;
        }
        String id = key.substring(prefix.length());
        if (!visitedIds.add(id)) {
          continue;
        }
        if (findById(id).isPresent()) {
          total++;
        }
      }
      if (keyScan.isFinished()) {
        break;
      }
      cursor = keyScan;
    }
    return total;
  }

  private String key(String id) {
    return sessionKeyPrefix() + id;
  }

  private String expiresKey(String id) {
    return expiresKeyPrefix() + id;
  }

  private String expirationsKey(long bucketEpochMinute) {
    return expirationsPrefix() + bucketEpochMinute;
  }

  private String sessionKeyPrefix() {
    return configuration.getRedisKeyPrefix() + "sessions:";
  }

  private String expiresKeyPrefix() {
    return configuration.getRedisKeyPrefix() + "sessions:expires:";
  }

  private String expirationsPrefix() {
    return configuration.getRedisKeyPrefix() + "sessions:expirations:";
  }

  private String createdChannel(String id) {
    return configuration.getRedisCreatedChannelPrefix() + id;
  }

  private long toTtlSeconds(RedisSession session) {
    return session.getMaxInactiveInterval()
        .map(Duration::getSeconds)
        .orElse(0L);
  }

  private Map<String, String> toHash(RedisSession session, Long bucketEpochMinute) {
    Map<String, String> map = new HashMap<>();
    map.put(FIELD_CREATION_TIME, String.valueOf(session.getCreationTime().toEpochMilli()));
    map.put(FIELD_LAST_ACCESSED_TIME, String.valueOf(session.getLastAccessedTime().toEpochMilli()));
    map.put(FIELD_IS_NEW, String.valueOf(session.isNew()));
    session.getMaxInactiveInterval().ifPresent(duration ->
        map.put(FIELD_MAX_INACTIVE_INTERVAL, String.valueOf(duration.getSeconds())));
    if (bucketEpochMinute != null) {
      map.put(FIELD_EXPIRATION_BUCKET_EPOCH_MINUTE, String.valueOf(bucketEpochMinute));
    }
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

  private Long readBucketEpochMinute(String sessionKey) {
    String raw = commands.hget(sessionKey, FIELD_EXPIRATION_BUCKET_EPOCH_MINUTE);
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return Long.parseLong(raw);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private Long toBucketEpochMinute(RedisSession session, long ttlSeconds) {
    Instant expiresAt = session.getLastAccessedTime().plusSeconds(ttlSeconds);
    return expiresAt.truncatedTo(ChronoUnit.MINUTES).getEpochSecond() / 60;
  }
}
