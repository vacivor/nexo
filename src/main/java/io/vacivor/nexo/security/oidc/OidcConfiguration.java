package io.vacivor.nexo.security.oidc;

import io.micronaut.context.annotation.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties("nexo.oidc")
public class OidcConfiguration {

  private String issuer = "http://localhost:8080";
  private String hmacSecret = "change-me";
  private Duration codeTtl = Duration.ofMinutes(5);
  private Duration accessTokenTtl = Duration.ofHours(1);
  private Duration idTokenTtl = Duration.ofHours(1);

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
}
