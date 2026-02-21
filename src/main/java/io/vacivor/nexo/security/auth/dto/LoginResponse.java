package io.vacivor.nexo.security.auth.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.util.Set;

@Serdeable
public class LoginResponse {

  private String principal;
  private Set<String> authorities;

  public LoginResponse() {
  }

  public LoginResponse(String principal, Set<String> authorities) {
    this.principal = principal;
    this.authorities = authorities;
  }

  public String getPrincipal() {
    return principal;
  }

  public void setPrincipal(String principal) {
    this.principal = principal;
  }

  public Set<String> getAuthorities() {
    return authorities;
  }

  public void setAuthorities(Set<String> authorities) {
    this.authorities = authorities;
  }
}
