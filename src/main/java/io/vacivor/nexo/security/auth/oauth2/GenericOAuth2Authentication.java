package io.vacivor.nexo.security.auth.oauth2;

public class GenericOAuth2Authentication extends OAuth2Authentication {

  public GenericOAuth2Authentication(String provider, String code, String redirectUri) {
    super(provider, code, redirectUri);
  }
}
