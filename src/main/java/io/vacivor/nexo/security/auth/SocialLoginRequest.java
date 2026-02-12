package io.vacivor.nexo.security.auth;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class SocialLoginRequest {

  private String provider;
  private String accessToken;

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }
}
