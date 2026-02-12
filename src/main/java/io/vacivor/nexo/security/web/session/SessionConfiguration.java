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
  private boolean cookieTransportEnabled = true;
  private boolean headerTransportEnabled = true;
  private boolean cookieSecure = false;
  private String cookieSameSite = "LAX";
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
