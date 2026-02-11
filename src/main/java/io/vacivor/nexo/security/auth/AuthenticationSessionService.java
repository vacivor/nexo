package io.vacivor.nexo.security.auth;

import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.http.cookie.Cookie;
import io.vacivor.nexo.security.web.session.Session;
import io.vacivor.nexo.security.web.session.SessionConfiguration;
import io.vacivor.nexo.security.web.session.SessionFixationStrategy;
import io.vacivor.nexo.security.web.session.SessionManager;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class AuthenticationSessionService {

  private static final String AUTH_SESSION_ATTRIBUTE = "AUTHENTICATION";

  public static String authenticationAttributeName() {
    return AUTH_SESSION_ATTRIBUTE;
  }

  private final SessionManager sessionManager;
  private final SessionConfiguration sessionConfiguration;

  public AuthenticationSessionService(SessionManager sessionManager,
      SessionConfiguration sessionConfiguration) {
    this.sessionManager = sessionManager;
    this.sessionConfiguration = sessionConfiguration;
  }

  public Session authenticate(Authentication authentication, MutableHttpResponse<?> response) {
    Session session = applySessionFixation();
    session.setAttribute(AUTH_SESSION_ATTRIBUTE, authentication);
    sessionManager.save(session);
    response.cookie(buildCookie(session.getId()));
    return session;
  }

  public Optional<Session> touchSession(String sessionId) {
    return sessionManager.findById(sessionId).map(session -> (Session) session);
  }

  public void clearSession(String sessionId, MutableHttpResponse<?> response) {
    sessionManager.deleteById(sessionId);
    response.cookie(buildExpiredCookie());
  }

  private Cookie buildCookie(String sessionId) {
    return Cookie.of(sessionConfiguration.getCookieName(), sessionId)
        .path("/")
        .httpOnly(true);
  }

  private Cookie buildExpiredCookie() {
    return Cookie.of(sessionConfiguration.getCookieName(), "")
        .path("/")
        .httpOnly(true)
        .maxAge(0);
  }

  private Session applySessionFixation() {
    SessionFixationStrategy strategy = sessionConfiguration.getSessionFixationStrategy();
    Optional<Session> existing = resolveExistingSession();
    if (strategy == SessionFixationStrategy.NONE) {
      return existing.orElseGet(sessionManager::createSession);
    }
    if (strategy == SessionFixationStrategy.NEW) {
      existing.ifPresent(session -> sessionManager.deleteById(session.getId()));
      return sessionManager.createSession();
    }
    if (existing.isPresent()) {
      Session previous = existing.get();
      Session migrated = sessionManager.createSession();
      copyAttributes(previous, migrated);
      previous.getMaxInactiveInterval().ifPresent(migrated::setMaxInactiveInterval);
      sessionManager.deleteById(previous.getId());
      return migrated;
    }
    return sessionManager.createSession();
  }

  private Optional<Session> resolveExistingSession() {
    return resolveSessionIdFromRequest().flatMap(sessionManager::findById);
  }

  private Optional<String> resolveSessionIdFromRequest() {
    return ServerRequestContext.currentRequest().flatMap(request -> {
      String headerName = sessionConfiguration.getHeaderName();
      if (headerName != null && !headerName.isBlank()) {
        String headerValue = request.getHeaders().get(headerName);
        if (headerValue != null && !headerValue.isBlank()) {
          return Optional.of(headerValue.trim());
        }
      }
      Cookie cookie = request.getCookies().get(sessionConfiguration.getCookieName());
      if (cookie != null && cookie.getValue() != null && !cookie.getValue().isBlank()) {
        return Optional.of(cookie.getValue());
      }
      return Optional.empty();
    });
  }

  private void copyAttributes(Session source, Session target) {
    for (String name : source.getAttributeNames()) {
      if (AUTH_SESSION_ATTRIBUTE.equals(name)) {
        continue;
      }
      target.setAttribute(name, source.getAttribute(name));
    }
  }
}
