package io.vacivor.nexo.security.auth.service;

import io.vacivor.nexo.security.auth.core.*;
import io.vacivor.nexo.security.auth.service.*;
import io.vacivor.nexo.security.auth.persistence.*;
import io.vacivor.nexo.security.auth.handler.*;
import io.vacivor.nexo.security.auth.password.*;
import io.vacivor.nexo.security.auth.provider.local.*;
import io.vacivor.nexo.security.auth.client.*;
import io.vacivor.nexo.security.auth.dto.*;
import io.vacivor.nexo.security.auth.web.*;

import jakarta.inject.Singleton;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class AuthenticationSessionCodec {

  public Object toSessionValue(Authentication authentication) {
    if (authentication == null) {
      return null;
    }
    return Map.of(
        "principal", authentication.getPrincipal(),
        "authorities", authentication.getAuthorities(),
        "attributes", authentication.getAttributes(),
        "authenticated", authentication.isAuthenticated()
    );
  }

  public Optional<Authentication> fromSessionValue(Object value) {
    if (value instanceof Authentication authentication) {
      return Optional.of(authentication);
    }
    if (value instanceof Map<?, ?> map) {
      return Optional.of(fromMap(map));
    }
    return Optional.empty();
  }

  private Authentication fromMap(Map<?, ?> map) {
    Object principal = map.get("principal");
    Set<String> authorities = Set.of();
    Object authoritiesObj = map.get("authorities");
    if (authoritiesObj instanceof Collection<?> collection) {
      authorities = collection.stream()
          .map(String::valueOf)
          .collect(Collectors.toSet());
    }
    Map<String, Object> attributes = Map.of();
    Object attributesObj = map.get("attributes");
    if (attributesObj instanceof Map<?, ?> attrMap) {
      attributes = attrMap.entrySet().stream()
          .collect(Collectors.toMap(e -> String.valueOf(e.getKey()), Map.Entry::getValue));
    }
    SimpleAuthentication authentication = new SimpleAuthentication(principal, authorities, attributes);
    Object authenticatedObj = map.get("authenticated");
    if (authenticatedObj instanceof Boolean authenticated) {
      authentication.setAuthenticated(authenticated);
    }
    return authentication;
  }
}