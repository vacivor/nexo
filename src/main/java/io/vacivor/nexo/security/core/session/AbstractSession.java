package io.vacivor.nexo.security.core.session;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractSession implements Session {

  private final String id;
  private final Map<String, Object> attributes = new ConcurrentHashMap<>();
  private Instant creationTime;
  private Instant lastAccessedTime;
  private Duration maxInactiveInterval;
  private boolean isNew = true;

  protected AbstractSession(String id) {
    this.id = id;
    Instant now = Instant.now();
    this.creationTime = now;
    this.lastAccessedTime = now;
  }

  protected AbstractSession(String id, Instant creationTime, Instant lastAccessedTime,
      Duration maxInactiveInterval, boolean isNew, Map<String, Object> attributes) {
    this.id = id;
    this.creationTime = creationTime;
    this.lastAccessedTime = lastAccessedTime;
    this.maxInactiveInterval = maxInactiveInterval;
    this.isNew = isNew;
    if (attributes != null && !attributes.isEmpty()) {
      this.attributes.putAll(attributes);
    }
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public Instant getCreationTime() {
    return creationTime;
  }

  @Override
  public void setCreationTime(Instant creationTime) {
    this.creationTime = creationTime;
  }

  @Override
  public Instant getLastAccessedTime() {
    return lastAccessedTime;
  }

  @Override
  public void setLastAccessedTime(Instant lastAccessedTime) {
    this.lastAccessedTime = lastAccessedTime;
  }

  @Override
  public Optional<Duration> getMaxInactiveInterval() {
    return Optional.ofNullable(maxInactiveInterval);
  }

  @Override
  public void setMaxInactiveInterval(Duration maxInactiveInterval) {
    this.maxInactiveInterval = maxInactiveInterval;
  }

  @Override
  public boolean isNew() {
    return isNew;
  }

  @Override
  public void setNew(boolean isNew) {
    this.isNew = isNew;
  }

  @Override
  public boolean isExpired() {
    if (maxInactiveInterval == null) {
      return false;
    }
    if (maxInactiveInterval.isZero() || maxInactiveInterval.isNegative()) {
      return true;
    }
    Instant base = lastAccessedTime != null ? lastAccessedTime : creationTime;
    if (base == null) {
      return false;
    }
    return base.plus(maxInactiveInterval).isBefore(Instant.now());
  }

  @Override
  public <T> Optional<T> getAttribute(String name, Class<T> type) {
    Object value = attributes.get(name);
    if (value == null) {
      return Optional.empty();
    }
    if (!type.isInstance(value)) {
      return Optional.empty();
    }
    return Optional.of(type.cast(value));
  }

  @Override
  public Object getAttribute(String name) {
    return attributes.get(name);
  }

  @Override
  public void setAttribute(String name, Object value) {
    if (value == null) {
      attributes.remove(name);
      return;
    }
    attributes.put(name, value);
  }

  @Override
  public Object removeAttribute(String name) {
    return attributes.remove(name);
  }

  @Override
  public Set<String> getAttributeNames() {
    return Collections.unmodifiableSet(attributes.keySet());
  }

  @Override
  public Map<String, Object> asMap() {
    return Collections.unmodifiableMap(attributes);
  }
}
