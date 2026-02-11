package io.vacivor.nexo.security.auth;

import io.micronaut.http.MutableHttpResponse;

public interface AuthenticationSuccessHandler {

  void onSuccess(Authentication authentication, MutableHttpResponse<?> response);
}
