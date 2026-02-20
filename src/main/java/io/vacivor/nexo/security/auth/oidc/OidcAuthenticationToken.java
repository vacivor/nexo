package io.vacivor.nexo.security.auth.oidc;

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
import io.vacivor.nexo.security.auth.core.AuthenticationToken;

@Serdeable
public class OidcAuthenticationToken implements AuthenticationToken {

  public static final String TYPE = "oidc";

  private String provider;
  private String idToken;

  public OidcAuthenticationToken() {
  }

  public OidcAuthenticationToken(String provider, String idToken) {
    this.provider = provider;
    this.idToken = idToken;
  }

  @Override
  public String getType() {
    return TYPE;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getIdToken() {
    return idToken;
  }

  public void setIdToken(String idToken) {
    this.idToken = idToken;
  }
}