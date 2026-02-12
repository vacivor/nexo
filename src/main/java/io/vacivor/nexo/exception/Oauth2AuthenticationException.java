package io.vacivor.nexo.exception;

public class Oauth2AuthenticationException extends AuthenticationException {

  public Oauth2AuthenticationException(String message) {
    super(message);
  }

  public Oauth2AuthenticationException(String message, Throwable cause) {
    super(message, cause);
  }
}
