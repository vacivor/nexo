package io.vacivor.nexo.oidc;

import io.micronaut.context.annotation.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties("nexo.oidc")
public class OidcConfiguration {

  private String issuer = "http://localhost:8080";
  private String hmacSecret = "change-me";
  private String signingAlgorithm = "RS256";
  private String rsaPrivateKey;
  private String rsaPublicKey;
  private String keyId = "nexo-oidc";
  private String loginPageUri = "/auth";
  private String oauthConsentPageUri = "/oauth/consent";
  private String oidcConsentPageUri = "/oidc/consent";
  private Duration codeTtl = Duration.ofMinutes(5);
  private Duration accessTokenTtl = Duration.ofHours(1);
  private Duration idTokenTtl = Duration.ofHours(1);
  private boolean refreshTokenEnabled = true;
  private Duration refreshTokenTtl = Duration.ofDays(30);

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  public String getHmacSecret() {
    return hmacSecret;
  }

  public void setHmacSecret(String hmacSecret) {
    this.hmacSecret = hmacSecret;
  }

  public String getSigningAlgorithm() {
    return signingAlgorithm;
  }

  public void setSigningAlgorithm(String signingAlgorithm) {
    this.signingAlgorithm = signingAlgorithm;
  }

  public String getRsaPrivateKey() {
    return rsaPrivateKey;
  }

  public void setRsaPrivateKey(String rsaPrivateKey) {
    this.rsaPrivateKey = rsaPrivateKey;
  }

  public String getRsaPublicKey() {
    return rsaPublicKey;
  }

  public void setRsaPublicKey(String rsaPublicKey) {
    this.rsaPublicKey = rsaPublicKey;
  }

  public String getKeyId() {
    return keyId;
  }

  public void setKeyId(String keyId) {
    this.keyId = keyId;
  }

  public String getLoginPageUri() {
    return loginPageUri;
  }

  public void setLoginPageUri(String loginPageUri) {
    this.loginPageUri = loginPageUri;
  }

  public String getOauthConsentPageUri() {
    return oauthConsentPageUri;
  }

  public void setOauthConsentPageUri(String oauthConsentPageUri) {
    this.oauthConsentPageUri = oauthConsentPageUri;
  }

  public String getOidcConsentPageUri() {
    return oidcConsentPageUri;
  }

  public void setOidcConsentPageUri(String oidcConsentPageUri) {
    this.oidcConsentPageUri = oidcConsentPageUri;
  }

  public Duration getCodeTtl() {
    return codeTtl;
  }

  public void setCodeTtl(Duration codeTtl) {
    this.codeTtl = codeTtl;
  }

  public Duration getAccessTokenTtl() {
    return accessTokenTtl;
  }

  public void setAccessTokenTtl(Duration accessTokenTtl) {
    this.accessTokenTtl = accessTokenTtl;
  }

  public Duration getIdTokenTtl() {
    return idTokenTtl;
  }

  public void setIdTokenTtl(Duration idTokenTtl) {
    this.idTokenTtl = idTokenTtl;
  }

  public boolean isRefreshTokenEnabled() {
    return refreshTokenEnabled;
  }

  public void setRefreshTokenEnabled(boolean refreshTokenEnabled) {
    this.refreshTokenEnabled = refreshTokenEnabled;
  }

  public Duration getRefreshTokenTtl() {
    return refreshTokenTtl;
  }

  public void setRefreshTokenTtl(Duration refreshTokenTtl) {
    this.refreshTokenTtl = refreshTokenTtl;
  }
}
