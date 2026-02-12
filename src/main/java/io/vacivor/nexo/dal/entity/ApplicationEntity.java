package io.vacivor.nexo.dal.entity;

import io.vacivor.nexo.TenantBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "applications")
public class ApplicationEntity extends TenantBaseEntity {

  @Column(name = "uuid", nullable = false, unique = true, updatable = false)
  private String uuid;

  @Column
  private String name;
  @Column
  private String description;
  @Column
  private String clientId;
  @Column
  private String clientSecret;
  @Column
  private String redirectUris;

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public String getRedirectUris() {
    return redirectUris;
  }

  public void setRedirectUris(String redirectUris) {
    this.redirectUris = redirectUris;
  }
}
