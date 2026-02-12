package io.vacivor.nexo.oidc.endpoints;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.uri.UriBuilder;
import io.vacivor.nexo.security.auth.Authentication;
import io.vacivor.nexo.security.auth.AuthenticationContext;
import io.vacivor.nexo.security.oidc.OidcAuthorizationCode;
import io.vacivor.nexo.security.oidc.OidcClient;
import io.vacivor.nexo.security.oidc.OidcConsentRequest;
import io.vacivor.nexo.security.oidc.OidcConsentService;
import io.vacivor.nexo.security.oidc.OidcService;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Controller
public class OidcAuthorizationEndpoint {

  private final OidcService oidcService;
  private final AuthenticationContext authenticationContext;
  private final OidcConsentService consentService;

  public OidcAuthorizationEndpoint(OidcService oidcService, AuthenticationContext authenticationContext,
      OidcConsentService consentService) {
    this.oidcService = oidcService;
    this.authenticationContext = authenticationContext;
    this.consentService = consentService;
  }

  @Get("/oidc/authorize")
  public HttpResponse<?> authorize(HttpRequest<?> request, @QueryValue("response_type") String responseType,
      @QueryValue("client_id") String clientId,
      @QueryValue("redirect_uri") String redirectUri,
      @QueryValue(value = "scope", defaultValue = "openid") String scope,
      @QueryValue(value = "state", defaultValue = "") String state,
      @QueryValue(value = "nonce", defaultValue = "") String nonce) {
    if (!"code".equals(responseType)) {
      return HttpResponse.badRequest();
    }
    Optional<OidcClient> client = oidcService.resolveClient(clientId);
    if (client.isEmpty() || !client.get().isRedirectUriAllowed(redirectUri)) {
      return HttpResponse.status(HttpStatus.UNAUTHORIZED);
    }
    Optional<Authentication> authentication = authenticationContext.getAuthentication();
    if (authentication.isEmpty()) {
      return HttpResponse.status(HttpStatus.UNAUTHORIZED);
    }
    String subject = String.valueOf(authentication.get().getPrincipal());
    Set<String> scopes = parseScopes(scope);
    if (!consentService.hasConsent(subject, clientId, scopes)) {
      Optional<OidcConsentRequest> pending = consentService.createPendingRequest(request, subject, clientId,
          redirectUri, scopes, state, nonce.isBlank() ? null : nonce);
      if (pending.isEmpty()) {
        return HttpResponse.status(HttpStatus.UNAUTHORIZED);
      }
      URI consentPage = UriBuilder.of("/oidc/consent")
          .queryParam("request_id", pending.get().getRequestId())
          .build();
      return HttpResponse.redirect(consentPage);
    }
    OidcAuthorizationCode code = oidcService.issueAuthorizationCode(clientId, redirectUri, subject, scopes,
        nonce.isBlank() ? null : nonce);
    URI location = UriBuilder.of(redirectUri)
        .queryParam("code", code.getCode())
        .queryParam("state", state)
        .build();
    return HttpResponse.redirect(location);
  }

  private Set<String> parseScopes(String scope) {
    if (scope == null || scope.isBlank()) {
      return Collections.emptySet();
    }
    return new HashSet<>(Arrays.asList(scope.trim().split("\\s+")));
  }
}
