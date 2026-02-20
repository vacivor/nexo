package io.vacivor.nexo.security.csrf;

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.util.AntPathMatcher;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import java.util.Map;
import org.reactivestreams.Publisher;

@Filter("/**")
public class CsrfFilter implements HttpServerFilter {

  private final CsrfService csrfService;
  private final CsrfConfiguration csrfConfiguration;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  public CsrfFilter(CsrfService csrfService, CsrfConfiguration csrfConfiguration) {
    this.csrfService = csrfService;
    this.csrfConfiguration = csrfConfiguration;
  }

  @Override
  public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
    if (!csrfConfiguration.isEnabled()) {
      return chain.proceed(request);
    }
    if (!requiresProtection(request)) {
      return chain.proceed(request);
    }
    String token = request.getHeaders().get(csrfConfiguration.getHeaderName());
    if ((token == null || token.isBlank()) && csrfConfiguration.getParameterName() != null
        && !csrfConfiguration.getParameterName().isBlank()) {
      token = request.getParameters().get(csrfConfiguration.getParameterName());
    }
    if (csrfService.validate(request, token)) {
      return chain.proceed(request);
    }
    MutableHttpResponse<Map<String, String>> response = HttpResponse.status(HttpStatus.FORBIDDEN)
        .body(Map.of("error", "invalid_csrf_token"));
    return Publishers.just(response);
  }

  private boolean requiresProtection(HttpRequest<?> request) {
    HttpMethod method = request.getMethod();
    if (method == HttpMethod.GET || method == HttpMethod.HEAD || method == HttpMethod.OPTIONS
        || method == HttpMethod.TRACE) {
      return false;
    }
    String path = request.getPath();
    if (path == null) {
      return false;
    }
    if (isExcluded(path)) {
      return false;
    }
    return path.startsWith("/api/") || "/login".equals(path) || "/register".equals(path);
  }

  private boolean isExcluded(String path) {
    for (String pattern : csrfConfiguration.getExcludePaths()) {
      if (pattern == null || pattern.isBlank()) {
        continue;
      }
      if (pathMatcher.matches(pattern.trim(), path)) {
        return true;
      }
    }
    return false;
  }
}
