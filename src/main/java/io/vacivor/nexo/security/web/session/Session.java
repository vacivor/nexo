package io.vacivor.nexo.security.web.session;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface Session {

  String getId();

  Instant getCreationTime();

  void setCreationTime(Instant creationTime);

  Instant getLastAccessedTime();

  void setLastAccessedTime(Instant lastAccessedTime);

  Optional<Duration> getMaxInactiveInterval();

  void setMaxInactiveInterval(Duration maxInactiveInterval);

  boolean isNew();

  void setNew(boolean isNew);

  boolean isExpired();

  <T> Optional<T> getAttribute(String name, Class<T> type);

  Object getAttribute(String name);

  void setAttribute(String name, Object value);

  Object removeAttribute(String name);

  Set<String> getAttributeNames();

  Map<String, Object> asMap();
}
