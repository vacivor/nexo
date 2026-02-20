package io.vacivor.nexo.security.web.session;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.vacivor.nexo.security.core.session.SessionFixationStrategy;
import io.vacivor.nexo.security.core.session.SessionSettings;
import java.time.Duration;

@ConfigurationProperties("nexo.security.session")
public class SessionConfiguration implements SessionSettings {

  private Duration maxInactiveInterval = Duration.ofMinutes(30);
  private int inMemoryMaximumSize = 10000;
  private String redisKeyPrefix = "nexo:";
  private String redisCreatedChannelPrefix = "nexo:sessions:channel:created:";
  private boolean redisKeyspaceNotificationsEnabled = true;
  private String redisExpiredEventsPattern = "__keyevent@*__:expired";
  private boolean redisLocalCacheEnabled = true;
  private int redisLocalCacheMaximumSize = 10000;
  private Duration redisLocalCacheTtl = Duration.ofSeconds(30);
  private int redisClusterIndexShards = 64;
  private String cookieName = "NEXO_SESSION";
  private String headerName = "X-Session-Id";
  private boolean cookieTransportEnabled = true;
  private boolean headerTransportEnabled = true;
  private boolean cookieSecure = false;
  private String cookieSameSite;
  private SessionFixationStrategy sessionFixationStrategy = SessionFixationStrategy.MIGRATE;

  public Duration getMaxInactiveInterval() {
    return maxInactiveInterval;
  }

  public void setMaxInactiveInterval(Duration maxInactiveInterval) {
    this.maxInactiveInterval = maxInactiveInterval;
  }

  public int getInMemoryMaximumSize() {
    return inMemoryMaximumSize;
  }

  public void setInMemoryMaximumSize(int inMemoryMaximumSize) {
    this.inMemoryMaximumSize = inMemoryMaximumSize;
  }

  public String getRedisKeyPrefix() {
    return redisKeyPrefix;
  }

  public void setRedisKeyPrefix(String redisKeyPrefix) {
    this.redisKeyPrefix = redisKeyPrefix;
  }

  public boolean isRedisKeyspaceNotificationsEnabled() {
    return redisKeyspaceNotificationsEnabled;
  }

  public void setRedisKeyspaceNotificationsEnabled(boolean redisKeyspaceNotificationsEnabled) {
    this.redisKeyspaceNotificationsEnabled = redisKeyspaceNotificationsEnabled;
  }

  public String getRedisCreatedChannelPrefix() {
    return redisCreatedChannelPrefix;
  }

  public void setRedisCreatedChannelPrefix(String redisCreatedChannelPrefix) {
    this.redisCreatedChannelPrefix = redisCreatedChannelPrefix;
  }

  public String getRedisExpiredEventsPattern() {
    return redisExpiredEventsPattern;
  }

  public void setRedisExpiredEventsPattern(String redisExpiredEventsPattern) {
    this.redisExpiredEventsPattern = redisExpiredEventsPattern;
  }

  public boolean isRedisLocalCacheEnabled() {
    return redisLocalCacheEnabled;
  }

  public void setRedisLocalCacheEnabled(boolean redisLocalCacheEnabled) {
    this.redisLocalCacheEnabled = redisLocalCacheEnabled;
  }

  public int getRedisLocalCacheMaximumSize() {
    return redisLocalCacheMaximumSize;
  }

  public void setRedisLocalCacheMaximumSize(int redisLocalCacheMaximumSize) {
    this.redisLocalCacheMaximumSize = redisLocalCacheMaximumSize;
  }

  public Duration getRedisLocalCacheTtl() {
    return redisLocalCacheTtl;
  }

  public void setRedisLocalCacheTtl(Duration redisLocalCacheTtl) {
    this.redisLocalCacheTtl = redisLocalCacheTtl;
  }

  public int getRedisClusterIndexShards() {
    return redisClusterIndexShards;
  }

  public void setRedisClusterIndexShards(int redisClusterIndexShards) {
    this.redisClusterIndexShards = redisClusterIndexShards;
  }

  public String getCookieName() {
    return cookieName;
  }

  public void setCookieName(String cookieName) {
    this.cookieName = cookieName;
  }

  public String getHeaderName() {
    return headerName;
  }

  public void setHeaderName(String headerName) {
    this.headerName = headerName;
  }

  public boolean isCookieTransportEnabled() {
    return cookieTransportEnabled;
  }

  public void setCookieTransportEnabled(boolean cookieTransportEnabled) {
    this.cookieTransportEnabled = cookieTransportEnabled;
  }

  public boolean isHeaderTransportEnabled() {
    return headerTransportEnabled;
  }

  public void setHeaderTransportEnabled(boolean headerTransportEnabled) {
    this.headerTransportEnabled = headerTransportEnabled;
  }

  public boolean isCookieSecure() {
    return cookieSecure;
  }

  public void setCookieSecure(boolean cookieSecure) {
    this.cookieSecure = cookieSecure;
  }

  public String getCookieSameSite() {
    return cookieSameSite;
  }

  public void setCookieSameSite(String cookieSameSite) {
    this.cookieSameSite = cookieSameSite;
  }

  public SessionFixationStrategy getSessionFixationStrategy() {
    return sessionFixationStrategy;
  }

  public void setSessionFixationStrategy(SessionFixationStrategy sessionFixationStrategy) {
    this.sessionFixationStrategy = sessionFixationStrategy;
  }
}
