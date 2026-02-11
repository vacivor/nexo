package io.vacivor.nexo.security.auth;

import io.micronaut.http.MutableHttpResponse;

public interface AuthenticationPersistence {

  void onSuccess(Authentication authentication, MutableHttpResponse<?> response);

  void onFailure(MutableHttpResponse<?> response);
}
