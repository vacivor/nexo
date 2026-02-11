package io.vacivor.nexo.security.auth;

import io.micronaut.serde.annotation.Serdeable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Serdeable
public class SimpleAuthentication implements Authentication {

  private Object principal;
  private Set<String> authorities = Set.of();
  private Map<String, Object> attributes = Map.of();
  private boolean authenticated = true;

  public SimpleAuthentication() {
  }

  public SimpleAuthentication(Object principal, Set<String> authorities, Map<String, Object> attributes) {
    this.principal = principal;
    if (authorities != null) {
      this.authorities = Set.copyOf(authorities);
    }
    if (attributes != null) {
      this.attributes = Map.copyOf(attributes);
    }
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }

  public void setPrincipal(Object principal) {
    this.principal = principal;
  }

  @Override
  public Set<String> getAuthorities() {
    return authorities == null ? Set.of() : Collections.unmodifiableSet(authorities);
  }

  public void setAuthorities(Set<String> authorities) {
    this.authorities = authorities == null ? Set.of() : Set.copyOf(authorities);
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes == null ? Map.of() : Collections.unmodifiableMap(attributes);
  }

  public void setAttributes(Map<String, Object> attributes) {
    this.attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
  }

  @Override
  public boolean isAuthenticated() {
    return authenticated;
  }

  public void setAuthenticated(boolean authenticated) {
    this.authenticated = authenticated;
  }

  public static SimpleAuthentication of(Object principal) {
    return new SimpleAuthentication(principal, Set.of(), Map.of());
  }
}
