package io.vacivor.nexo.dal.entity;

import io.vacivor.nexo.security.providers.IdentityProviderProtocol;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "identity_providers")
public class IdentityProviderEntity extends BaseEntity {

  @Column(name = "uuid", nullable = false, unique = true, updatable = false)
  private String uuid;

  @Enumerated(EnumType.STRING)
  @Column(name = "protocol", nullable = false)
  private IdentityProviderProtocol protocol;

  @Column(name = "provider", nullable = false)
  private String provider;

  @Column(name = "display_name")
  private String displayName;

  @Column(name = "enabled", nullable = false)
  private Boolean enabled = Boolean.TRUE;

  @Column(name = "client_id")
  private String clientId;

  @Column(name = "client_secret")
  private String clientSecret;

  @Column(name = "authorization_uri")
  private String authorizationUri;

  @Column(name = "token_uri")
  private String tokenUri;

  @Column(name = "userinfo_uri")
  private String userInfoUri;

  @Column(name = "jwks_uri")
  private String jwksUri;

  @Column(name = "issuer")
  private String issuer;

  @Column(name = "redirect_uri")
  private String redirectUri;

  @Column(name = "scopes")
  private String scopes;

  @Column(name = "extra_config", length = 8000)
  private String extraConfig;

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public IdentityProviderProtocol getProtocol() {
    return protocol;
  }

  public void setProtocol(IdentityProviderProtocol protocol) {
    this.protocol = protocol;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
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

  public String getAuthorizationUri() {
    return authorizationUri;
  }

  public void setAuthorizationUri(String authorizationUri) {
    this.authorizationUri = authorizationUri;
  }

  public String getTokenUri() {
    return tokenUri;
  }

  public void setTokenUri(String tokenUri) {
    this.tokenUri = tokenUri;
  }

  public String getUserInfoUri() {
    return userInfoUri;
  }

  public void setUserInfoUri(String userInfoUri) {
    this.userInfoUri = userInfoUri;
  }

  public String getJwksUri() {
    return jwksUri;
  }

  public void setJwksUri(String jwksUri) {
    this.jwksUri = jwksUri;
  }

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }

  public String getScopes() {
    return scopes;
  }

  public void setScopes(String scopes) {
    this.scopes = scopes;
  }

  public String getExtraConfig() {
    return extraConfig;
  }

  public void setExtraConfig(String extraConfig) {
    this.extraConfig = extraConfig;
  }
}
