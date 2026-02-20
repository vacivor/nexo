package io.vacivor.nexo.security.auth.persistence;

import io.vacivor.nexo.security.auth.core.*;
import io.vacivor.nexo.security.auth.service.*;
import io.vacivor.nexo.security.auth.persistence.*;
import io.vacivor.nexo.security.auth.handler.*;
import io.vacivor.nexo.security.auth.password.*;
import io.vacivor.nexo.security.auth.provider.local.*;
import io.vacivor.nexo.security.auth.client.*;
import io.vacivor.nexo.security.auth.dto.*;
import io.vacivor.nexo.security.auth.web.*;

import io.micronaut.http.MutableHttpResponse;
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