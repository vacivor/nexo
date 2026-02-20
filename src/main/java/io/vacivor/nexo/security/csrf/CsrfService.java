package io.vacivor.nexo.security.csrf;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.context.ServerRequestContext;
import io.vacivor.nexo.security.core.session.Session;
import jakarta.inject.Singleton;

@Singleton
public class CsrfService {

  private final CsrfTokenRepository csrfTokenRepository;
  private final CsrfConfiguration csrfConfiguration;

  public CsrfService(CsrfTokenRepository csrfTokenRepository, CsrfConfiguration csrfConfiguration) {
    this.csrfTokenRepository = csrfTokenRepository;
    this.csrfConfiguration = csrfConfiguration;
  }

  public String issueToken(MutableHttpResponse<?> response) {
    HttpRequest<?> request = ServerRequestContext.currentRequest()
        .orElseThrow(() -> new IllegalStateException("No current request available for CSRF token issuing"));
    CsrfToken csrfToken = csrfTokenRepository.loadToken(request)
        .orElseGet(() -> csrfTokenRepository.generateToken(request));
    csrfTokenRepository.saveToken(csrfToken, request, response);
    response.header(csrfConfiguration.getHeaderName(), csrfToken.getToken());
    return csrfToken.getToken();
  }

  public boolean validate(HttpRequest<?> request, String token) {
    if (token == null || token.isBlank()) {
      return false;
    }
    return csrfTokenRepository.loadToken(request)
        .map(CsrfToken::getToken)
        .map(token::equals)
        .orElse(false);
  }

  public String rotateToken(Session session, MutableHttpResponse<?> response) {
    CsrfToken csrfToken = new CsrfToken(csrfConfiguration.getHeaderName(), csrfConfiguration.getParameterName(),
        java.util.UUID.randomUUID().toString());
    boolean savedBySession = false;
    try {
      csrfTokenRepository.saveToken(csrfToken, session);
      savedBySession = true;
    } catch (UnsupportedOperationException ignored) {
      // fall back to request/response repository APIs (e.g. cookie-based repository)
    }
    if (!savedBySession) {
      HttpRequest<?> request = ServerRequestContext.currentRequest()
          .orElseThrow(() -> new IllegalStateException("No current request available for CSRF token rotating"));
      csrfTokenRepository.saveToken(csrfToken, request, response);
    }
    response.header(csrfConfiguration.getHeaderName(), csrfToken.getToken());
    return csrfToken.getToken();
  }
}
