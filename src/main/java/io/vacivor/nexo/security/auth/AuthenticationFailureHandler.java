package io.vacivor.nexo.security.auth;

import io.micronaut.http.MutableHttpResponse;

public interface AuthenticationFailureHandler {

  void onFailure(MutableHttpResponse<?> response);
}
