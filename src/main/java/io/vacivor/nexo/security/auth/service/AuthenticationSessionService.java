package io.vacivor.nexo.security.auth.service;

import io.vacivor.nexo.security.auth.core.*;
import io.vacivor.nexo.security.auth.service.*;
import io.vacivor.nexo.security.auth.persistence.*;
import io.vacivor.nexo.security.auth.handler.*;
import io.vacivor.nexo.security.auth.password.*;
import io.vacivor.nexo.security.auth.provider.local.*;
import io.vacivor.nexo.security.auth.client.*;
import io.vacivor.nexo.security.auth.dto.*;
import io.vacivor.nexo.security.auth.web.*;

import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.SameSite;
import io.vacivor.nexo.security.config.SecurityConfiguration;
import io.vacivor.nexo.security.csrf.CsrfConfiguration;
import io.vacivor.nexo.security.csrf.CsrfService;
import io.vacivor.nexo.security.core.session.Session;
import io.vacivor.nexo.security.core.session.SessionSettings;
import io.vacivor.nexo.security.core.session.SessionFixationStrategy;
import io.vacivor.nexo.security.core.session.SessionManager;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class AuthenticationSessionService {

  private static final String AUTH_SESSION_ATTRIBUTE = "AUTHENTICATION";

  public static String authenticationAttributeName() {
    return AUTH_SESSION_ATTRIBUTE;
  }

  private final SessionManager sessionManager;
  private final SessionSettings sessionConfiguration;
  private final SecurityConfiguration securityConfiguration;
  private final AuthenticationSessionCodec authenticationSessionCodec;
  private final CsrfService csrfService;
  private final CsrfConfiguration csrfConfiguration;

  public AuthenticationSessionService(SessionManager sessionManager,
      SessionSettings sessionConfiguration,
      SecurityConfiguration securityConfiguration,
      AuthenticationSessionCodec authenticationSessionCodec,
      CsrfService csrfService,
      CsrfConfiguration csrfConfiguration) {
    this.sessionManager = sessionManager;
    this.sessionConfiguration = sessionConfiguration;
    this.securityConfiguration = securityConfiguration;
    this.authenticationSessionCodec = authenticationSessionCodec;
    this.csrfService = csrfService;
    this.csrfConfiguration = csrfConfiguration;
  }

  public Session authenticate(Authentication authentication, MutableHttpResponse<?> response) {
    Session session = applySessionFixation();
    session.setAttribute(AUTH_SESSION_ATTRIBUTE, authenticationSessionCodec.toSessionValue(authentication));
    sessionManager.save(session);
    String csrfToken = csrfService.rotateToken(session, response);
    response.header(csrfConfiguration.getHeaderName(), csrfToken);
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
      configured = securityConfiguration.getCookieSameSite();
    }
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