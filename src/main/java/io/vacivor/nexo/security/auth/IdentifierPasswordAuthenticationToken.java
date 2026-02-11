package io.vacivor.nexo.security.auth;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class IdentifierPasswordAuthenticationToken implements AuthenticationToken {

  public static final String TYPE = "identifier_password";

  private String identifier;
  private String password;

  public IdentifierPasswordAuthenticationToken() {
  }

  public IdentifierPasswordAuthenticationToken(String identifier, String password) {
    this.identifier = identifier;
    this.password = password;
  }

  @Override
  public String getType() {
    return TYPE;
  }

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
