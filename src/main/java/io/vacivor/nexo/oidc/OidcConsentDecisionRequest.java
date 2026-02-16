package io.vacivor.nexo.oidc;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class OidcConsentDecisionRequest {

  private String requestId;
  private String csrfToken;
  private boolean approve;

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

  public boolean isApprove() {
    return approve;
  }

  public void setApprove(boolean approve) {
    this.approve = approve;
  }
}
