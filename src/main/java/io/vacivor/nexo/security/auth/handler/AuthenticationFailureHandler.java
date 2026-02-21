package io.vacivor.nexo.security.auth.handler;

import io.micronaut.http.MutableHttpResponse;

public interface AuthenticationFailureHandler {

  void onFailure(MutableHttpResponse<?> response);
}
