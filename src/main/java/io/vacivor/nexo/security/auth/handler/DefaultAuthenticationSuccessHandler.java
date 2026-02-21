package io.vacivor.nexo.security.auth.handler;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.MutableHttpResponse;
import io.vacivor.nexo.security.auth.core.Authentication;
import jakarta.inject.Singleton;

@Singleton
@Requires(missingBeans = AuthenticationSuccessHandler.class)
public class DefaultAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  @Override
  public void onSuccess(Authentication authentication, MutableHttpResponse<?> response) {
    // no-op by default
  }
}
