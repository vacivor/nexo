package io.vacivor.nexo.security.auth.oauth2;

public class GithubOAuth2Authentication extends OAuth2Authentication {

  public static final String PROVIDER = "github";

  public GithubOAuth2Authentication(String code, String redirectUri) {
    super(PROVIDER, code, redirectUri);
  }
}
