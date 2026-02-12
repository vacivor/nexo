package io.vacivor.nexo.security.auth;

import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.SameSite;
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
  private final AuthenticationSessionCodec authenticationSessionCodec;

  public AuthenticationSessionService(SessionManager sessionManager,
      SessionConfiguration sessionConfiguration,
      AuthenticationSessionCodec authenticationSessionCodec) {
    this.sessionManager = sessionManager;
    this.sessionConfiguration = sessionConfiguration;
    this.authenticationSessionCodec = authenticationSessionCodec;
  }

  public Session authenticate(Authentication authentication, MutableHttpResponse<?> response) {
    Session session = applySessionFixation();
    session.setAttribute(AUTH_SESSION_ATTRIBUTE, authenticationSessionCodec.toSessionValue(authentication));
    sessionManager.save(session);
    if (sessionConfiguration.isCookieTransportEnabled()) {
      response.cookie(buildCookie(session.getId()));
    }
    return session;
  }

  public Optional<Session> touchSession(String sessionId) {
    return sessionManager.findById(sessionId).map(session -> (Session) session);
  }

  public void clearSession(String sessionId, MutableHttpResponse<?> response) {
    sessionManager.deleteById(sessionId);
    if (sessionConfiguration.isCookieTransportEnabled()) {
      response.cookie(buildExpiredCookie());
    }
  }

  private Cookie buildCookie(String sessionId) {
    Cookie cookie = Cookie.of(sessionConfiguration.getCookieName(), sessionId)
        .path("/")
        .httpOnly(true)
        .secure(sessionConfiguration.isCookieSecure());
    SameSite sameSite = resolveSameSite();
    if (sameSite != null) {
      cookie.sameSite(sameSite);
    }
    return cookie;
  }

  private Cookie buildExpiredCookie() {
    Cookie cookie = Cookie.of(sessionConfiguration.getCookieName(), "")
        .path("/")
        .httpOnly(true)
        .secure(sessionConfiguration.isCookieSecure())
        .maxAge(0);
    SameSite sameSite = resolveSameSite();
    if (sameSite != null) {
      cookie.sameSite(sameSite);
    }
    return cookie;
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
      if (sessionConfiguration.isHeaderTransportEnabled()) {
        String headerName = sessionConfiguration.getHeaderName();
        if (headerName != null && !headerName.isBlank()) {
          String headerValue = request.getHeaders().get(headerName);
          if (headerValue != null && !headerValue.isBlank()) {
            return Optional.of(headerValue.trim());
          }
        }
      }
      if (!sessionConfiguration.isCookieTransportEnabled()) {
        return Optional.empty();
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

  private SameSite resolveSameSite() {
    String configured = sessionConfiguration.getCookieSameSite();
    if (configured == null || configured.isBlank()) {
      return null;
    }
    try {
      return SameSite.valueOf(configured.trim().toUpperCase());
    } catch (IllegalArgumentException ignored) {
      return null;
    }
  }
}
