package io.vacivor.nexo.security.web.session.redis;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.ScanArgs;
import io.micronaut.context.annotation.Requires;
import io.vacivor.nexo.security.web.session.SessionAttributesCodec;
import io.vacivor.nexo.security.web.session.SessionConfiguration;
import io.vacivor.nexo.security.web.session.SessionRepository;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Singleton
@Requires(property = "nexo.session.store", value = "redis")
public class RedisSessionRepository implements SessionRepository<RedisSession> {

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
  public RedisSession createSession(String id) {
    RedisSession session = new RedisSession(id);
    session.setMaxInactiveInterval(configuration.getMaxInactiveInterval());
    return session;
  }

  @Override
  public Optional<RedisSession> findById(String id) {
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
  public RedisSession save(RedisSession session) {
    commands.hset(key(session.getId()), toHash(session));
    long ttlSeconds = toTtlSeconds(session);
    if (ttlSeconds > 0) {
      commands.expire(key(session.getId()), ttlSeconds);
    }
    return session;
  }

  @Override
  public void deleteById(String id) {
    commands.del(key(id));
  }

  @Override
  public List<RedisSession> findAllSessions() {
    return activeSessions();
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
    String prefix = configuration.getRedisKeyPrefix();
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
    String prefix = configuration.getRedisKeyPrefix();
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

  private List<RedisSession> activeSessions() {
    List<RedisSession> all = new ArrayList<>();
    Set<String> visitedIds = new HashSet<>();
    String prefix = configuration.getRedisKeyPrefix();
    ScanArgs scanArgs = ScanArgs.Builder.matches(prefix + "*").limit(500);
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
        findById(id).ifPresent(all::add);
      }
      if (keyScan.isFinished()) {
        break;
      }
      cursor = keyScan;
    }
    return all;
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
