package io.vacivor.nexo.dal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "oidc_refresh_tokens")
public class OidcRefreshTokenEntity extends BaseEntity {

  @Column(name = "token", nullable = false, unique = true, length = 1024)
  private String token;

  @Column(name = "subject", nullable = false)
  private String subject;

  @Column(name = "client_id", nullable = false)
  private String clientId;

  @Column(name = "scopes", nullable = false, length = 2048)
  private String scopes;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "family_id")
  private String familyId;

  @Column(name = "consumed", nullable = false)
  private boolean consumed;

  @Column(name = "revoked", nullable = false)
  private boolean revoked;

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getScopes() {
    return scopes;
  }

  public void setScopes(String scopes) {
    this.scopes = scopes;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Instant expiresAt) {
    this.expiresAt = expiresAt;
  }

  public String getFamilyId() {
    return familyId;
  }

  public void setFamilyId(String familyId) {
    this.familyId = familyId;
  }

  public boolean isConsumed() {
    return consumed;
  }

  public void setConsumed(boolean consumed) {
    this.consumed = consumed;
  }

  public boolean isRevoked() {
    return revoked;
  }

  public void setRevoked(boolean revoked) {
    this.revoked = revoked;
  }
}
