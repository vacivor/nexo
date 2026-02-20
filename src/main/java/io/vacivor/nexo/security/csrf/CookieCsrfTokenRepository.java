package io.vacivor.nexo.security.csrf;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.SameSite;
import io.vacivor.nexo.security.config.SecurityConfiguration;
import io.vacivor.nexo.security.core.session.SessionTransportSettings;
import jakarta.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

@Singleton
@Requires(property = "nexo.security.csrf.repository", value = "cookie")
public class CookieCsrfTokenRepository implements CsrfTokenRepository {

  private final CsrfConfiguration csrfConfiguration;
  private final SecurityConfiguration securityConfiguration;
  private final SessionTransportSettings sessionConfiguration;

  public CookieCsrfTokenRepository(CsrfConfiguration csrfConfiguration,
      SecurityConfiguration securityConfiguration,
      SessionTransportSettings sessionConfiguration) {
    this.csrfConfiguration = csrfConfiguration;
    this.securityConfiguration = securityConfiguration;
    this.sessionConfiguration = sessionConfiguration;
  }

  @Override
  public CsrfToken generateToken(HttpRequest<?> request) {
    return new CsrfToken(csrfConfiguration.getHeaderName(), csrfConfiguration.getParameterName(),
        UUID.randomUUID().toString());
  }

  @Override
  public Optional<CsrfToken> loadToken(HttpRequest<?> request) {
    Cookie cookie = request.getCookies().get(csrfConfiguration.getCookieName());
    if (cookie == null || cookie.getValue() == null || cookie.getValue().isBlank()) {
      return Optional.empty();
    }
    return Optional.of(new CsrfToken(csrfConfiguration.getHeaderName(),
        csrfConfiguration.getParameterName(),
        cookie.getValue()));
  }

  @Override
  public void saveToken(CsrfToken csrfToken, HttpRequest<?> request, MutableHttpResponse<?> response) {
    response.cookie(buildCookie(csrfToken));
  }

  private Cookie buildCookie(CsrfToken csrfToken) {
    boolean secure = csrfConfiguration.getCookieSecure() != null
        ? csrfConfiguration.getCookieSecure()
        : false;
    String value = csrfToken == null ? "" : csrfToken.getToken();
    Cookie cookie = Cookie.of(csrfConfiguration.getCookieName(), value)
        .path(csrfConfiguration.getCookiePath() == null || csrfConfiguration.getCookiePath().isBlank()
            ? "/"
            : csrfConfiguration.getCookiePath())
        .httpOnly(csrfConfiguration.isCookieHttpOnly())
        .secure(secure);
    if (csrfToken == null) {
      cookie.maxAge(0);
    }
    String sameSite = csrfConfiguration.getCookieSameSite();
    if (sameSite == null || sameSite.isBlank()) {
      sameSite = securityConfiguration.getCookieSameSite();
    }
    if (sameSite == null || sameSite.isBlank()) {
      sameSite = sessionConfiguration.getCookieSameSite();
    }
    if (sameSite != null && !sameSite.isBlank()) {
      try {
        cookie.sameSite(SameSite.valueOf(sameSite.trim().toUpperCase()));
      } catch (IllegalArgumentException ignored) {
      }
    }
    return cookie;
  }
}
