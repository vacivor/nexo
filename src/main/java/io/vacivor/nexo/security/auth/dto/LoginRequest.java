package io.vacivor.nexo.security.auth.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class LoginRequest {

  private String identifier;
  private String password;

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
