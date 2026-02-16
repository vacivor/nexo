package io.vacivor.nexo.core;

import java.time.Instant;
import java.util.Set;

public interface RefreshToken {

  String getToken();

  String getSubject();

  String getClientId();

  Set<String> getScopes();

  Instant getExpiresAt();
}
