package io.vacivor.nexo.oidc;

import io.vacivor.nexo.core.AccessToken;
import java.time.Instant;
import java.util.Set;

public class OidcAccessToken implements AccessToken {

  private final String token;
  private final String subject;
  private final String clientId;
  private final Set<String> scopes;
  private final Instant expiresAt;
  private final String familyId;

  public OidcAccessToken(String token, String subject, String clientId,
      Set<String> scopes, Instant expiresAt) {
    this(token, subject, clientId, scopes, expiresAt, null);
  }

  public OidcAccessToken(String token, String subject, String clientId,
      Set<String> scopes, Instant expiresAt, String familyId) {
    this.token = token;
    this.subject = subject;
    this.clientId = clientId;
    this.scopes = scopes;
    this.expiresAt = expiresAt;
    this.familyId = familyId;
  }

  public String getToken() {
    return token;
  }

  public String getSubject() {
    return subject;
  }

  public String getClientId() {
    return clientId;
  }

  public Set<String> getScopes() {
    return scopes;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public String getFamilyId() {
    return familyId;
  }
}
