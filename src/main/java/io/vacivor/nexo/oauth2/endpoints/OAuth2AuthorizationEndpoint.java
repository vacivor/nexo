package io.vacivor.nexo.oauth2.endpoints;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.uri.UriBuilder;
import io.vacivor.nexo.authorizationserver.authorization.AuthorizationService;
import io.vacivor.nexo.authorizationserver.client.AuthorizationClientService;
import io.vacivor.nexo.oidc.OidcAuthorizationCode;
import io.vacivor.nexo.oidc.OidcClient;
import io.vacivor.nexo.security.auth.core.Authentication;
import io.vacivor.nexo.security.auth.core.AuthenticationContext;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Controller
@Requires(property = "nexo.oauth2.enabled", value = "true", defaultValue = "false")
public class OAuth2AuthorizationEndpoint {

  private final AuthorizationClientService authorizationClientService;
  private final AuthorizationService authorizationService;
  private final AuthenticationContext authenticationContext;

  public OAuth2AuthorizationEndpoint(AuthorizationClientService authorizationClientService,
      AuthorizationService authorizationService,
      AuthenticationContext authenticationContext) {
    this.authorizationClientService = authorizationClientService;
    this.authorizationService = authorizationService;
    this.authenticationContext = authenticationContext;
  }

  @Get(uri = "/oauth/authorize")
  public HttpResponse<?> authorize(@QueryValue("response_type") String responseType,
      @QueryValue("client_id") String clientId,
      @QueryValue("redirect_uri") String redirectUri,
      @QueryValue(value = "scope", defaultValue = "") String scope,
      @QueryValue(value = "state", defaultValue = "") String state,
      @QueryValue(value = "nonce", defaultValue = "") String nonce) {
    if (!"code".equals(responseType)) {
      return HttpResponse.badRequest();
    }
    Optional<OidcClient> client = authorizationClientService.resolveClient(clientId);
    if (client.isEmpty() || !client.get().isRedirectUriAllowed(redirectUri)) {
      return HttpResponse.status(HttpStatus.UNAUTHORIZED);
    }
    Optional<Authentication> authentication = authenticationContext.getAuthentication();
    if (authentication.isEmpty()) {
      return HttpResponse.status(HttpStatus.UNAUTHORIZED);
    }
    String subject = String.valueOf(authentication.get().getPrincipal());
    if (!authorizationService.isUserTenantAllowedForClient(subject, clientId)) {
      return errorRedirect(redirectUri, "access_denied", state, "tenant_mismatch");
    }
    Set<String> scopes = parseScopes(scope);
    OidcAuthorizationCode code = authorizationService.issueAuthorizationCode(clientId, redirectUri, subject,
        scopes, nonce.isBlank() ? null : nonce);
    URI location = UriBuilder.of(redirectUri)
        .queryParam("code", code.getCode())
        .queryParam("state", state)
        .build();
    return temporaryRedirect(location);
  }

  private Set<String> parseScopes(String scope) {
    if (scope == null || scope.isBlank()) {
      return Collections.emptySet();
    }
    return new HashSet<>(Arrays.asList(scope.trim().split("\\s+")));
  }

  private HttpResponse<?> errorRedirect(String redirectUri, String error, String state,
      String errorDescription) {
    if (redirectUri == null || redirectUri.isBlank()) {
      return HttpResponse.status(HttpStatus.BAD_REQUEST);
    }
    UriBuilder builder = UriBuilder.of(redirectUri)
        .queryParam("error", error)
        .queryParam("state", state);
    if (errorDescription != null && !errorDescription.isBlank()) {
      builder.queryParam("error_description", errorDescription);
    }
    return temporaryRedirect(builder.build());
  }

  private HttpResponse<?> temporaryRedirect(URI location) {
    return HttpResponse.status(HttpStatus.FOUND).headers(headers -> headers.location(location));
  }
}
