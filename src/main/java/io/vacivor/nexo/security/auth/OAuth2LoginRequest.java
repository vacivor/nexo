package io.vacivor.nexo.security.auth;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class OAuth2LoginRequest {

  private String provider;
  private String code;
  private String redirectUri;

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }
}
