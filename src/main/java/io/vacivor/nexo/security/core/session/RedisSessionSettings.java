package io.vacivor.nexo.security.core.session;

import java.time.Duration;

public interface RedisSessionSettings {

  Duration getMaxInactiveInterval();

  String getRedisKeyPrefix();

  String getRedisCreatedChannelPrefix();

  boolean isRedisKeyspaceNotificationsEnabled();

  String getRedisExpiredEventsPattern();

  boolean isRedisLocalCacheEnabled();

  int getRedisLocalCacheMaximumSize();

  Duration getRedisLocalCacheTtl();

  int getRedisClusterIndexShards();
}
