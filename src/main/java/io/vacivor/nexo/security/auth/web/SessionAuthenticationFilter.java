package io.vacivor.nexo.security.auth.web;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.vacivor.nexo.security.auth.core.Authentication;
import io.vacivor.nexo.security.auth.core.SecurityContext;
import io.vacivor.nexo.security.auth.service.AuthenticationSessionCodec;
import io.vacivor.nexo.security.auth.service.AuthenticationSessionService;
import io.vacivor.nexo.security.core.session.Session;
import io.vacivor.nexo.security.core.session.SessionTransportSettings;
import io.vacivor.nexo.security.core.session.SessionManager;
import java.util.Optional;
import org.reactivestreams.Publisher;

@Filter("/**")
public class SessionAuthenticationFilter implements HttpServerFilter {

  private final SessionManager sessionManager;
  private final SessionTransportSettings sessionConfiguration;
  private final AuthenticationSessionCodec authenticationSessionCodec;

  public SessionAuthenticationFilter(SessionManager sessionManager,
      SessionTransportSettings sessionConfiguration,
      AuthenticationSessionCodec authenticationSessionCodec) {
    this.sessionManager = sessionManager;
    this.sessionConfiguration = sessionConfiguration;
    this.authenticationSessionCodec = authenticationSessionCodec;
  }

  @Override
  public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
    resolveSession(request).ifPresent(session -> {
      Authentication authentication = authenticationSessionCodec
          .fromSessionValue(session.getAttribute(AuthenticationSessionService.authenticationAttributeName()))
          .orElse(null);
      if (authentication != null) {
        SecurityContext.setAuthentication(request, authentication);
      }
    });
    return chain.proceed(request);
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
    if (cookie == null) {
      return Optional.empty();
    }
    Optional<? extends Session> session = sessionManager.findById(cookie.getValue());
    if (session.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(session.get());
  }
}
