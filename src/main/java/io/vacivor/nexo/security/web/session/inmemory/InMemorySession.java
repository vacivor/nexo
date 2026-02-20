package io.vacivor.nexo.security.web.session.inmemory;

import io.vacivor.nexo.security.core.session.AbstractSession;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public class InMemorySession extends AbstractSession {

  public InMemorySession(String id) {
    super(id);
  }

  public InMemorySession(String id, Instant creationTime, Instant lastAccessedTime,
      Duration maxInactiveInterval, boolean isNew, Map<String, Object> attributes) {
    super(id, creationTime, lastAccessedTime, maxInactiveInterval, isNew, attributes);
  }
}
