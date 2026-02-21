package io.vacivor.nexo.security.auth.handler;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import jakarta.inject.Singleton;

@Singleton
@Requires(missingBeans = AuthenticationFailureHandler.class)
public class DefaultAuthenticationFailureHandler implements AuthenticationFailureHandler {

  @Override
  public void onFailure(MutableHttpResponse<?> response) {
    response.status(HttpStatus.UNAUTHORIZED);
  }
}
