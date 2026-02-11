package io.vacivor.nexo.security.web.session.redis;

import io.vacivor.nexo.security.web.session.AbstractSession;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public class RedisSession extends AbstractSession {

  public RedisSession(String id) {
    super(id);
  }

  public RedisSession(String id, Instant creationTime, Instant lastAccessedTime,
      Duration maxInactiveInterval, boolean isNew, Map<String, Object> attributes) {
    super(id, creationTime, lastAccessedTime, maxInactiveInterval, isNew, attributes);
  }
}
