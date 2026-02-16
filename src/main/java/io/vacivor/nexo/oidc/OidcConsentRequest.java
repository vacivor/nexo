package io.vacivor.nexo.oidc;

import java.util.Set;

public class OidcConsentRequest {

  private final String requestId;
  private final String csrfToken;
  private final String subject;
  private final String clientId;
  private final String redirectUri;
  private final Set<String> scopes;
  private final String state;
  private final String nonce;

  public OidcConsentRequest(String requestId, String csrfToken, String subject, String clientId,
      String redirectUri, Set<String> scopes, String state, String nonce) {
    this.requestId = requestId;
    this.csrfToken = csrfToken;
    this.subject = subject;
    this.clientId = clientId;
    this.redirectUri = redirectUri;
    this.scopes = scopes;
    this.state = state;
    this.nonce = nonce;
  }

  public String getRequestId() {
    return requestId;
  }

  public String getCsrfToken() {
    return csrfToken;
  }

  public String getSubject() {
    return subject;
  }

  public String getClientId() {
    return clientId;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public Set<String> getScopes() {
    return scopes;
  }

  public String getState() {
    return state;
  }

  public String getNonce() {
    return nonce;
  }
}
