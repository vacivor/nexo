package io.vacivor.nexo.dal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "oidc_consents")
public class OidcConsentEntity extends BaseEntity {

  @Column(name = "subject", nullable = false)
  private String subject;

  @Column(name = "client_id", nullable = false)
  private String clientId;

  @Column(name = "scopes", nullable = false)
  private String scopes;

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
}
