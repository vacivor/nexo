package io.vacivor.nexo.security.web.session.db;

import io.vacivor.nexo.security.web.session.AbstractSession;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public class DatabaseSession extends AbstractSession {

  public DatabaseSession(String id) {
    super(id);
  }

  public DatabaseSession(String id, Instant creationTime, Instant lastAccessedTime,
      Duration maxInactiveInterval, boolean isNew, Map<String, Object> attributes) {
    super(id, creationTime, lastAccessedTime, maxInactiveInterval, isNew, attributes);
  }
}
