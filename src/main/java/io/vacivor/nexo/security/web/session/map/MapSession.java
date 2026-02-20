package io.vacivor.nexo.security.web.session.map;

import io.vacivor.nexo.security.core.session.AbstractSession;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public class MapSession extends AbstractSession {

  public MapSession(String id) {
    super(id);
  }

  public MapSession(String id, Instant creationTime, Instant lastAccessedTime,
      Duration maxInactiveInterval, boolean isNew, Map<String, Object> attributes) {
    super(id, creationTime, lastAccessedTime, maxInactiveInterval, isNew, attributes);
  }
}
