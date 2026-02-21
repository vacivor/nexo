package io.vacivor.nexo.security.auth.persistence;

import io.micronaut.http.MutableHttpResponse;
import io.vacivor.nexo.security.auth.core.Authentication;
import io.vacivor.nexo.security.auth.service.AuthenticationSessionService;
import jakarta.inject.Singleton;

@Singleton
public class SessionAuthenticationPersistence implements AuthenticationPersistence {

  private final AuthenticationSessionService authenticationSessionService;

  public SessionAuthenticationPersistence(AuthenticationSessionService authenticationSessionService) {
    this.authenticationSessionService = authenticationSessionService;
  }

  @Override
  public void onSuccess(Authentication authentication, MutableHttpResponse<?> response) {
    authenticationSessionService.authenticate(authentication, response);
  }

  @Override
  public void onFailure(MutableHttpResponse<?> response) {
    // no-op by default
  }
}
