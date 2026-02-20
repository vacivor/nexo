package io.vacivor.nexo.security.web.session.redis;

import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.pubsub.RedisClusterPubSubAdapter;
import io.lettuce.core.cluster.pubsub.StatefulRedisClusterPubSubConnection;
import io.lettuce.core.cluster.models.partitions.RedisClusterNode;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.vacivor.nexo.security.web.session.SessionConfiguration;
import io.vacivor.nexo.security.web.session.events.SessionCreatedEvent;
import io.vacivor.nexo.security.web.session.events.SessionDeletedEvent;
import io.vacivor.nexo.security.web.session.events.SessionDestroyedEvent;
import io.vacivor.nexo.security.web.session.events.SessionExpiredEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Requires(property = "nexo.security.session.store", value = "redis")
@Requires(beans = StatefulRedisClusterConnection.class)
@Requires(beans = RedisClusterClient.class)
public class RedisClusterSessionExpirationListener {

  private static final Logger LOG = LoggerFactory.getLogger(RedisClusterSessionExpirationListener.class);
  private static final String KEYSPACE_PREFIX = "__keyspace@";

  private final RedisClusterClient clusterClient;
  private final SessionConfiguration sessionConfiguration;
  private final ApplicationEventPublisher<Object> eventPublisher;
  private final RedisSessionLocalCache localCache;

  private StatefulRedisClusterPubSubConnection<String, String> pubSubConnection;

  public RedisClusterSessionExpirationListener(RedisClusterClient clusterClient,
      SessionConfiguration sessionConfiguration,
      ApplicationEventPublisher<Object> eventPublisher,
      RedisSessionLocalCache localCache) {
    this.clusterClient = clusterClient;
    this.sessionConfiguration = sessionConfiguration;
    this.eventPublisher = eventPublisher;
    this.localCache = localCache;
  }

  @PostConstruct
  void start() {
    if (!sessionConfiguration.isRedisKeyspaceNotificationsEnabled()) {
      return;
    }
    this.pubSubConnection = clusterClient.connectPubSub();
    this.pubSubConnection.addListener(new RedisClusterPubSubAdapter<>() {
      @Override
      public void message(RedisClusterNode node, String channel, String message) {
        handleNotification(channel, message);
      }
    });
    String keyspacePattern = resolveKeyspacePattern();
    String createdPattern = resolveCreatedPattern();
    pubSubConnection.sync().psubscribe(keyspacePattern, createdPattern);
    LOG.info("Subscribed Redis cluster session listener to keyspace pattern {} and created pattern {}",
        keyspacePattern, createdPattern);
  }

  @PreDestroy
  void stop() {
    if (pubSubConnection != null) {
      try {
        pubSubConnection.close();
      } catch (Exception ignored) {
      }
    }
  }

  private String resolveKeyspacePattern() {
    String configured = sessionConfiguration.getRedisExpiredEventsPattern();
    if (configured != null && !configured.isBlank()) {
      return configured;
    }
    return KEYSPACE_PREFIX + "*__:" + sessionConfiguration.getRedisKeyPrefix() + "*";
  }

  private String resolveCreatedPattern() {
    return sessionConfiguration.getRedisCreatedChannelPrefix() + "*";
  }

  private void handleNotification(String channel, String message) {
    if (channel != null && channel.startsWith(sessionConfiguration.getRedisCreatedChannelPrefix())) {
      String sessionId = message;
      if (sessionId == null || sessionId.isBlank()) {
        sessionId = channel.substring(sessionConfiguration.getRedisCreatedChannelPrefix().length());
      }
      if (sessionId != null && !sessionId.isBlank()) {
        localCache.invalidate(sessionId);
        eventPublisher.publishEvent(new SessionCreatedEvent(this, new RedisSession(sessionId)));
      }
      return;
    }
    if (channel != null && channel.startsWith(KEYSPACE_PREFIX)) {
      if (!"expired".equals(message) && !"del".equals(message)) {
        return;
      }
      int marker = channel.indexOf("__:");
      if (marker < 0 || marker + 3 >= channel.length()) {
        return;
      }
      handleEvent(channel.substring(marker + 3), message);
      return;
    }
    if (channel != null && channel.startsWith("__keyevent@") && channel.endsWith(":expired")) {
      handleEvent(message, "expired");
      return;
    }
    if (channel != null && channel.startsWith("__keyevent@") && channel.endsWith(":del")) {
      handleEvent(message, "del");
    }
  }

  private void handleEvent(String key, String type) {
    if (key == null || key.isBlank()) {
      return;
    }
    String expiresPrefix = expiresKeyPrefix();
    String sessionPrefix = sessionKeyPrefix();
    if ("expired".equals(type) && key.startsWith(expiresPrefix)) {
      String sessionId = key.substring(expiresPrefix.length());
      if (sessionId.isBlank()) {
        return;
      }
      RedisSession session = new RedisSession(sessionId);
      localCache.invalidate(sessionId);
      eventPublisher.publishEvent(new SessionExpiredEvent(this, session));
      eventPublisher.publishEvent(new SessionDestroyedEvent(this, session));
      return;
    }
    if ("del".equals(type) && key.startsWith(sessionPrefix)) {
      String sessionId = key.substring(sessionPrefix.length());
      if (sessionId.isBlank()) {
        return;
      }
      RedisSession session = new RedisSession(sessionId);
      localCache.invalidate(sessionId);
      eventPublisher.publishEvent(new SessionDeletedEvent(this, session));
      eventPublisher.publishEvent(new SessionDestroyedEvent(this, session));
    }
  }

  private String sessionKeyPrefix() {
    return sessionConfiguration.getRedisKeyPrefix() + "sessions:";
  }

  private String expiresKeyPrefix() {
    return sessionConfiguration.getRedisKeyPrefix() + "sessions:expires:";
  }
}
