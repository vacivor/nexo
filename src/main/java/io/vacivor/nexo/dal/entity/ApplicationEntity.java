package io.vacivor.nexo.dal.entity;

import io.vacivor.nexo.TenantBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "applications",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_applications_client_id_deleted_at",
            columnNames = {"client_id", "deleted_at"})
    })
public class ApplicationEntity extends TenantBaseEntity {

  @Column(name = "uuid", nullable = false, unique = true, updatable = false)
  private String uuid;

  @Column
  private String name;
  @Column
  private String description;
  @Column(name = "logo")
  private String logo;
  @Column(name = "client_type")
  private String clientType;
  @Column(name = "client_id")
  private String clientId;
  @Column
  private String clientSecret;
  @Column
  private String redirectUris;
  @Column(name = "id_token_expiration")
  private Integer idTokenExpiration;
  @Column(name = "refresh_token_expiration")
  private Integer refreshTokenExpiration;

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

  public String getLogo() {
    return logo;
  }

  public void setLogo(String logo) {
    this.logo = logo;
  }

  public String getClientType() {
    return clientType;
  }

  public void setClientType(String clientType) {
    this.clientType = clientType;
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

  public Integer getIdTokenExpiration() {
    return idTokenExpiration;
  }

  public void setIdTokenExpiration(Integer idTokenExpiration) {
    this.idTokenExpiration = idTokenExpiration;
  }

  public Integer getRefreshTokenExpiration() {
    return refreshTokenExpiration;
  }

  public void setRefreshTokenExpiration(Integer refreshTokenExpiration) {
    this.refreshTokenExpiration = refreshTokenExpiration;
  }
}
