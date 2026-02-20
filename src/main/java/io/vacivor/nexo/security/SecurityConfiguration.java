package io.vacivor.nexo.security;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("nexo.security")
public class SecurityConfiguration {

  private String passwordEncoder = "plain";
  private String cookieSameSite = "LAX";

  public String getPasswordEncoder() {
    return passwordEncoder;
  }

  public void setPasswordEncoder(String passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  public String getCookieSameSite() {
    return cookieSameSite;
  }

  public void setCookieSameSite(String cookieSameSite) {
    this.cookieSameSite = cookieSameSite;
  }
}
