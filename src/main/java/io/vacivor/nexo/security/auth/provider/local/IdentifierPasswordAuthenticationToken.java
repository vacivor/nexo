package io.vacivor.nexo.security.auth.provider.local;

import io.vacivor.nexo.security.auth.core.*;
import io.vacivor.nexo.security.auth.service.*;
import io.vacivor.nexo.security.auth.persistence.*;
import io.vacivor.nexo.security.auth.handler.*;
import io.vacivor.nexo.security.auth.password.*;
import io.vacivor.nexo.security.auth.provider.local.*;
import io.vacivor.nexo.security.auth.client.*;
import io.vacivor.nexo.security.auth.dto.*;
import io.vacivor.nexo.security.auth.web.*;

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