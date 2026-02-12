package io.vacivor.nexo.security.oidc;

import java.time.Instant;
import java.util.Set;

public class OidcAuthorizationCode {

  private final String code;
  private final String clientId;
  private final String redirectUri;
  private final String subject;
  private final Set<String> scopes;
  private final String nonce;
  private final Instant expiresAt;

  public OidcAuthorizationCode(String code, String clientId, String redirectUri, String subject,
      Set<String> scopes, String nonce, Instant expiresAt) {
    this.code = code;
    this.clientId = clientId;
    this.redirectUri = redirectUri;
    this.subject = subject;
    this.scopes = scopes;
    this.nonce = nonce;
    this.expiresAt = expiresAt;
  }

  public String getCode() {
    return code;
  }

  public String getClientId() {
    return clientId;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public String getSubject() {
    return subject;
  }

  public Set<String> getScopes() {
    return scopes;
  }

  public String getNonce() {
    return nonce;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }
}
