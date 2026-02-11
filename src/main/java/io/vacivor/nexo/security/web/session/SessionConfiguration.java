package io.vacivor.nexo.security.web.session;

import io.micronaut.context.annotation.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties("nexo.session")
public class SessionConfiguration {

  private Duration maxInactiveInterval = Duration.ofMinutes(30);
  private int inMemoryMaximumSize = 10000;
  private String redisKeyPrefix = "nexo:sessions:";

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
}
