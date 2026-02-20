package io.vacivor.nexo.security.core.session;

import java.time.Duration;

public interface SessionSettings extends SessionTransportSettings, RedisSessionSettings {

  SessionFixationStrategy getSessionFixationStrategy();

  int getInMemoryMaximumSize();
}
