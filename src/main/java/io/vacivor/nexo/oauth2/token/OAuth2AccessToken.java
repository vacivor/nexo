package io.vacivor.nexo.oauth2.token;

import io.vacivor.nexo.core.AccessToken;
import java.time.Instant;
import java.util.Set;

public class OAuth2AccessToken implements AccessToken {

  private final String token;
  private final String subject;
  private final String clientId;
  private final Set<String> scopes;
  private final Instant expiresAt;
  private final String familyId;

  public OAuth2AccessToken(String token, String subject, String clientId,
      Set<String> scopes, Instant expiresAt) {
    this(token, subject, clientId, scopes, expiresAt, null);
  }

  public OAuth2AccessToken(String token, String subject, String clientId,
      Set<String> scopes, Instant expiresAt, String familyId) {
    this.token = token;
    this.subject = subject;
    this.clientId = clientId;
    this.scopes = scopes;
    this.expiresAt = expiresAt;
    this.familyId = familyId;
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

  public String getFamilyId() {
    return familyId;
  }
}
