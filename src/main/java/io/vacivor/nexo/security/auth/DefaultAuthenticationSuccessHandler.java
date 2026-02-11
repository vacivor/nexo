package io.vacivor.nexo.security.auth;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.MutableHttpResponse;
import jakarta.inject.Singleton;

@Singleton
@Requires(missingBeans = AuthenticationSuccessHandler.class)
public class DefaultAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  @Override
  public void onSuccess(Authentication authentication, MutableHttpResponse<?> response) {
    // no-op by default
  }
}
