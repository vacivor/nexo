package io.vacivor.nexo.security.auth;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.vacivor.nexo.security.web.session.Session;
import io.vacivor.nexo.security.web.session.SessionConfiguration;
import io.vacivor.nexo.security.web.session.SessionManager;
import org.reactivestreams.Publisher;
import java.util.Optional;

@Filter("/**")
public class SessionAuthenticationFilter implements HttpServerFilter {

  private final SessionManager sessionManager;
  private final SessionConfiguration sessionConfiguration;

  public SessionAuthenticationFilter(SessionManager sessionManager,
      SessionConfiguration sessionConfiguration) {
    this.sessionManager = sessionManager;
    this.sessionConfiguration = sessionConfiguration;
  }

  @Override
  public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
    resolveSession(request).ifPresent(session -> {
      Authentication authentication = session.getAttribute(AuthenticationSessionService.authenticationAttributeName(), Authentication.class)
          .orElse(null);
      if (authentication != null) {
        SecurityContext.setAuthentication(request, authentication);
      }
    });
    return chain.proceed(request);
  }

  private Optional<Session> resolveSession(HttpRequest<?> request) {
    String headerName = sessionConfiguration.getHeaderName();
    if (headerName != null && !headerName.isBlank()) {
      String headerValue = request.getHeaders().get(headerName);
      if (headerValue != null && !headerValue.isBlank()) {
        return sessionManager.findById(headerValue.trim()).map(session -> (Session) session);
      }
    }
    Cookie cookie = request.getCookies().get(sessionConfiguration.getCookieName());
    if (cookie == null) {
      return Optional.empty();
    }
    return sessionManager.findById(cookie.getValue()).map(session -> (Session) session);
  }
}
