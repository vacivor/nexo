package io.vacivor.nexo.security.auth.handler;

import io.vacivor.nexo.security.auth.core.*;
import io.vacivor.nexo.security.auth.service.*;
import io.vacivor.nexo.security.auth.persistence.*;
import io.vacivor.nexo.security.auth.handler.*;
import io.vacivor.nexo.security.auth.password.*;
import io.vacivor.nexo.security.auth.provider.local.*;
import io.vacivor.nexo.security.auth.client.*;
import io.vacivor.nexo.security.auth.dto.*;
import io.vacivor.nexo.security.auth.web.*;

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