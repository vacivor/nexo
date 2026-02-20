package io.vacivor.nexo.security.auth.handler;

import io.micronaut.http.MutableHttpResponse;
import io.vacivor.nexo.security.auth.core.Authentication;

public interface AuthenticationSuccessHandler {

  void onSuccess(Authentication authentication, MutableHttpResponse<?> response);
}