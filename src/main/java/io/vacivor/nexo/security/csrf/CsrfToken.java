package io.vacivor.nexo.security.csrf;

public class CsrfToken {

  private final String headerName;
  private final String parameterName;
  private final String token;

  public CsrfToken(String headerName, String parameterName, String token) {
    this.headerName = headerName;
    this.parameterName = parameterName;
    this.token = token;
  }

  public String getHeaderName() {
    return headerName;
  }

  public String getParameterName() {
    return parameterName;
  }

  public String getToken() {
    return token;
  }
}
