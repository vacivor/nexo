package io.vacivor.nexo.security.csrf;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.vacivor.nexo.security.web.session.Session;
import java.util.Optional;

public interface CsrfTokenRepository {

  CsrfToken generateToken(HttpRequest<?> request);

  Optional<CsrfToken> loadToken(HttpRequest<?> request);

  void saveToken(CsrfToken csrfToken, HttpRequest<?> request, MutableHttpResponse<?> response);

  default void saveToken(CsrfToken csrfToken, Session session) {
    throw new UnsupportedOperationException("Session-based saveToken is not supported");
  }
}
