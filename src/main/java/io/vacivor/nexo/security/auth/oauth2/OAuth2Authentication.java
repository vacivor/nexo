package io.vacivor.nexo.security.auth.oauth2;

import io.vacivor.nexo.security.auth.core.*;
import io.vacivor.nexo.security.auth.service.*;
import io.vacivor.nexo.security.auth.persistence.*;
import io.vacivor.nexo.security.auth.handler.*;
import io.vacivor.nexo.security.auth.password.*;
import io.vacivor.nexo.security.auth.provider.local.*;
import io.vacivor.nexo.security.auth.client.*;
import io.vacivor.nexo.security.auth.dto.*;
import io.vacivor.nexo.security.auth.web.*;

import io.vacivor.nexo.security.auth.core.AuthenticationToken;

public abstract class OAuth2Authentication implements AuthenticationToken {

  public static final String TYPE = "oauth2_code";

  private final String provider;
  private final String code;
  private final String redirectUri;

  protected OAuth2Authentication(String provider, String code, String redirectUri) {
    this.provider = provider;
    this.code = code;
    this.redirectUri = redirectUri;
  }

  @Override
  public String getType() {
    return TYPE;
  }

  public String getProvider() {
    return provider;
  }

  public String getCode() {
    return code;
  }

  public String getRedirectUri() {
    return redirectUri;
  }
}