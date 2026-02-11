package io.vacivor.nexo.security.web.session;

import io.micronaut.context.annotation.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties("nexo.session")
public class SessionConfiguration {

  private Duration maxInactiveInterval = Duration.ofMinutes(30);
  private int inMemoryMaximumSize = 10000;
  private String redisKeyPrefix = "nexo:sessions:";
  private String cookieName = "NEXO_SESSION";
  private String headerName = "X-Session-Id";
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

  public SessionFixationStrategy getSessionFixationStrategy() {
    return sessionFixationStrategy;
  }

  public void setSessionFixationStrategy(SessionFixationStrategy sessionFixationStrategy) {
    this.sessionFixationStrategy = sessionFixationStrategy;
  }
}
