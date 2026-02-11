package io.vacivor.nexo.security.auth;

import io.micronaut.http.MutableHttpResponse;
import io.vacivor.nexo.security.web.session.Session;
import io.vacivor.nexo.security.web.session.SessionConfiguration;
import io.vacivor.nexo.security.web.session.SessionManager;
import jakarta.inject.Singleton;

@Singleton
public class TokenAuthenticationPersistence implements AuthenticationPersistence {

  private static final String AUTH_SESSION_ATTRIBUTE = AuthenticationSessionService.authenticationAttributeName();

  private final SessionManager sessionManager;
  private final SessionConfiguration sessionConfiguration;

  public TokenAuthenticationPersistence(SessionManager sessionManager,
      SessionConfiguration sessionConfiguration) {
    this.sessionManager = sessionManager;
    this.sessionConfiguration = sessionConfiguration;
  }

  @Override
  public void onSuccess(Authentication authentication, MutableHttpResponse<?> response) {
    Session session = sessionManager.createSession();
    session.setAttribute(AUTH_SESSION_ATTRIBUTE, authentication);
    sessionManager.save(session);
    response.header(sessionConfiguration.getHeaderName(), session.getId());
  }

  @Override
  public void onFailure(MutableHttpResponse<?> response) {
    // no-op
  }
}
