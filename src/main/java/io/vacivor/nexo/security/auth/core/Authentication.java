package io.vacivor.nexo.security.auth.core;

import java.util.Map;
import java.util.Set;

public interface Authentication {

  Object getPrincipal();

  Set<String> getAuthorities();

  Map<String, Object> getAttributes();

  boolean isAuthenticated();
}
