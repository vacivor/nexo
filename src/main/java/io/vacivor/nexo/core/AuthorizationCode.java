package io.vacivor.nexo.core;

import java.time.Instant;
import java.util.Set;

public interface AuthorizationCode {

  String getCode();

  String getClientId();

  String getRedirectUri();

  String getSubject();

  Set<String> getScopes();

  Instant getExpiresAt();

  String getNonce();
}
