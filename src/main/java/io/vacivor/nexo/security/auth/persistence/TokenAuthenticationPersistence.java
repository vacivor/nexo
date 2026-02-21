package io.vacivor.nexo.security.auth.persistence;

import io.micronaut.http.MutableHttpResponse;
import io.vacivor.nexo.security.auth.core.Authentication;
import io.vacivor.nexo.security.auth.service.AuthenticationSessionService;
import io.vacivor.nexo.security.core.session.Session;
import io.vacivor.nexo.security.core.session.SessionTransportSettings;
import io.vacivor.nexo.security.core.session.SessionManager;
import jakarta.inject.Singleton;

@Singleton
public class TokenAuthenticationPersistence implements AuthenticationPersistence {

  private static final String AUTH_SESSION_ATTRIBUTE = AuthenticationSessionService.authenticationAttributeName();

  private final SessionManager sessionManager;
  private final SessionTransportSettings sessionConfiguration;

  public TokenAuthenticationPersistence(SessionManager sessionManager,
      SessionTransportSettings sessionConfiguration) {
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
