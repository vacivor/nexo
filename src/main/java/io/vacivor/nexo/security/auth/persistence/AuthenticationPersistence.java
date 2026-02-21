package io.vacivor.nexo.security.auth.persistence;

import io.micronaut.http.MutableHttpResponse;
import io.vacivor.nexo.security.auth.core.Authentication;

public interface AuthenticationPersistence {

  void onSuccess(Authentication authentication, MutableHttpResponse<?> response);

  void onFailure(MutableHttpResponse<?> response);
}
