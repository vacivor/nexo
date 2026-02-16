package io.vacivor.nexo.core;

import java.util.Set;

public interface RegisteredClient {

  String getClientId();

  String getClientSecret();

  Set<String> getRedirectUris();

  Set<String> getScopes();

  boolean isConfidential();
}
