package io.vacivor.nexo.security.csrf;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.SameSite;
import io.micronaut.context.annotation.Requires;
import io.vacivor.nexo.security.config.SecurityConfiguration;
import io.vacivor.nexo.security.core.session.Session;
import io.vacivor.nexo.security.core.session.SessionTransportSettings;
import io.vacivor.nexo.security.core.session.SessionManager;
import jakarta.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

@Singleton
@Requires(property = "nexo.security.csrf.repository", value = "session", defaultValue = "session")
public class SessionCsrfTokenRepository implements CsrfTokenRepository {

  private final SessionManager sessionManager;
  private final SessionTransportSettings sessionConfiguration;
  private final SecurityConfiguration securityConfiguration;
  private final CsrfConfiguration csrfConfiguration;

  public SessionCsrfTokenRepository(SessionManager sessionManager,
      SessionTransportSettings sessionConfiguration,
      SecurityConfiguration securityConfiguration,
      CsrfConfiguration csrfConfiguration) {
    this.sessionManager = sessionManager;
    this.sessionConfiguration = sessionConfiguration;
    this.securityConfiguration = securityConfiguration;
    this.csrfConfiguration = csrfConfiguration;
  }

  @Override
  public CsrfToken generateToken(HttpRequest<?> request) {
    return new CsrfToken(csrfConfiguration.getHeaderName(), csrfConfiguration.getParameterName(),
        UUID.randomUUID().toString());
  }

  @Override
  public Optional<CsrfToken> loadToken(HttpRequest<?> request) {
    return resolveSession(request)
        .map(session -> session.getAttribute(csrfConfiguration.getSessionAttributeName()))
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .filter(token -> !token.isBlank())
        .map(token -> new CsrfToken(csrfConfiguration.getHeaderName(),
            csrfConfiguration.getParameterName(),
            token));
  }

  @Override
  public void saveToken(CsrfToken csrfToken, HttpRequest<?> request, MutableHttpResponse<?> response) {
    Session session = resolveSession(request).orElseGet(sessionManager::createSession);
    saveToken(csrfToken, session);
    writeSessionTransport(response, session);
  }

  @Override
  public void saveToken(CsrfToken csrfToken, Session session) {
    if (csrfToken == null) {
      session.removeAttribute(csrfConfiguration.getSessionAttributeName());
    } else {
      session.setAttribute(csrfConfiguration.getSessionAttributeName(), csrfToken.getToken());
    }
    sessionManager.save(session);
  }

  private Optional<Session> resolveSession(HttpRequest<?> request) {
    if (sessionConfiguration.isHeaderTransportEnabled()) {
      String headerName = sessionConfiguration.getHeaderName();
      if (headerName != null && !headerName.isBlank()) {
        String headerValue = request.getHeaders().get(headerName);
        if (headerValue != null && !headerValue.isBlank()) {
          Optional<Session> byHeader = sessionManager.findById(headerValue.trim())
              .map(session -> (Session) session);
          if (byHeader.isPresent()) {
            return byHeader;
          }
        }
      }
    }
    if (!sessionConfiguration.isCookieTransportEnabled()) {
      return Optional.empty();
    }
    Cookie cookie = request.getCookies().get(sessionConfiguration.getCookieName());
    if (cookie == null || cookie.getValue() == null || cookie.getValue().isBlank()) {
      return Optional.empty();
    }
    return sessionManager.findById(cookie.getValue()).map(session -> (Session) session);
  }

  private void writeSessionTransport(MutableHttpResponse<?> response, Session session) {
    if (sessionConfiguration.isHeaderTransportEnabled()) {
      response.header(sessionConfiguration.getHeaderName(), session.getId());
    }
    if (sessionConfiguration.isCookieTransportEnabled()) {
      response.cookie(buildSessionCookie(session.getId()));
    }
  }

  private Cookie buildSessionCookie(String sessionId) {
    Cookie cookie = Cookie.of(sessionConfiguration.getCookieName(), sessionId)
        .path("/")
        .httpOnly(true)
        .secure(sessionConfiguration.isCookieSecure());
    String configured = sessionConfiguration.getCookieSameSite();
    if (configured == null || configured.isBlank()) {
      configured = securityConfiguration.getCookieSameSite();
    }
    if (configured != null && !configured.isBlank()) {
      try {
        cookie.sameSite(SameSite.valueOf(configured.trim().toUpperCase()));
      } catch (IllegalArgumentException ignored) {
      }
    }
    return cookie;
  }
}
