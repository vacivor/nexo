package io.vacivor.nexo.oidc;

import io.vacivor.nexo.core.RefreshToken;
import java.time.Instant;
import java.util.Set;

public class OidcRefreshToken implements RefreshToken {

  private final String token;
  private final String subject;
  private final String clientId;
  private final Set<String> scopes;
  private final Instant expiresAt;

  public OidcRefreshToken(String token, String subject, String clientId,
      Set<String> scopes, Instant expiresAt) {
    this.token = token;
    this.subject = subject;
    this.clientId = clientId;
    this.scopes = scopes;
    this.expiresAt = expiresAt;
  }

  @Override
  public String getToken() {
    return token;
  }

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public String getClientId() {
    return clientId;
  }

  @Override
  public Set<String> getScopes() {
    return scopes;
  }

  @Override
  public Instant getExpiresAt() {
    return expiresAt;
  }
}
