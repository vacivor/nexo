package io.vacivor.nexo.oidc;

import io.micronaut.serde.annotation.Serdeable;
import java.util.Set;

@Serdeable
public class OidcAuthorizeResponse {

  private String requestId;
  private String csrfToken;
  private String clientId;
  private Set<String> scopes;
  private String state;

  public OidcAuthorizeResponse() {
  }

  public OidcAuthorizeResponse(String requestId, String csrfToken, String clientId,
      Set<String> scopes, String state) {
    this.requestId = requestId;
    this.csrfToken = csrfToken;
    this.clientId = clientId;
    this.scopes = scopes;
    this.state = state;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getCsrfToken() {
    return csrfToken;
  }

  public void setCsrfToken(String csrfToken) {
    this.csrfToken = csrfToken;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public Set<String> getScopes() {
    return scopes;
  }

  public void setScopes(Set<String> scopes) {
    this.scopes = scopes;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }
}
