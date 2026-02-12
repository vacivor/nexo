package io.vacivor.nexo.security.auth;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class OidcLoginRequest {

  private String provider;
  private String idToken;

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
