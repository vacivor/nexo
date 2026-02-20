package io.vacivor.nexo.security.auth.social;

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
public class SocialAuthenticationToken implements AuthenticationToken {

  public static final String TYPE = "social";

  private String provider;
  private String accessToken;

  public SocialAuthenticationToken() {
  }

  public SocialAuthenticationToken(String provider, String accessToken) {
    this.provider = provider;
    this.accessToken = accessToken;
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

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }
}