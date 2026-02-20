package io.vacivor.nexo.security.csrf;

import io.micronaut.context.annotation.ConfigurationProperties;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("nexo.security.csrf")
public class CsrfConfiguration {

  private boolean enabled = true;
  private String repository = "session";
  private String headerName = "X-CSRF-Token";
  private String parameterName = "_csrf";
  private String sessionAttributeName = "CSRF_TOKEN";
  private String cookieName = "XSRF-TOKEN";
  private String cookiePath = "/";
  private boolean cookieHttpOnly = false;
  private Boolean cookieSecure;
  private String cookieSameSite;
  private List<String> excludePaths = new ArrayList<>(List.of(
      "/csrf",
      "/oauth/token",
      "/oidc/token"
  ));

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getRepository() {
    return repository;
  }

  public void setRepository(String repository) {
    this.repository = repository;
  }

  public String getHeaderName() {
    return headerName;
  }

  public void setHeaderName(String headerName) {
    this.headerName = headerName;
  }

  public String getParameterName() {
    return parameterName;
  }

  public void setParameterName(String parameterName) {
    this.parameterName = parameterName;
  }

  public String getSessionAttributeName() {
    return sessionAttributeName;
  }

  public void setSessionAttributeName(String sessionAttributeName) {
    this.sessionAttributeName = sessionAttributeName;
  }

  public String getCookieName() {
    return cookieName;
  }

  public void setCookieName(String cookieName) {
    this.cookieName = cookieName;
  }

  public String getCookiePath() {
    return cookiePath;
  }

  public void setCookiePath(String cookiePath) {
    this.cookiePath = cookiePath;
  }

  public boolean isCookieHttpOnly() {
    return cookieHttpOnly;
  }

  public void setCookieHttpOnly(boolean cookieHttpOnly) {
    this.cookieHttpOnly = cookieHttpOnly;
  }

  public Boolean getCookieSecure() {
    return cookieSecure;
  }

  public void setCookieSecure(Boolean cookieSecure) {
    this.cookieSecure = cookieSecure;
  }

  public String getCookieSameSite() {
    return cookieSameSite;
  }

  public void setCookieSameSite(String cookieSameSite) {
    this.cookieSameSite = cookieSameSite;
  }

  public List<String> getExcludePaths() {
    return excludePaths;
  }

  public void setExcludePaths(List<String> excludePaths) {
    this.excludePaths = excludePaths == null ? new ArrayList<>() : new ArrayList<>(excludePaths);
  }
}
