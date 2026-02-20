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
import io.vacivor.nexo.oidc.OidcAuthorizationCode;
import io.vacivor.nexo.oidc.OidcClient;
import io.vacivor.nexo.oidc.OidcConfiguration;
import io.vacivor.nexo.oidc.OidcConsentRequest;
import io.vacivor.nexo.oidc.OidcConsentService;
import io.vacivor.nexo.oidc.OidcService;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class OidcAuthorizationEndpoint {

  private static final Logger LOG = LoggerFactory.getLogger(OidcAuthorizationEndpoint.class);

  private final OidcService oidcService;
  private final AuthenticationContext authenticationContext;
  private final OidcConsentService consentService;
  private final OidcConfiguration oidcConfiguration;

  public OidcAuthorizationEndpoint(OidcService oidcService, AuthenticationContext authenticationContext,
      OidcConsentService consentService,
      OidcConfiguration oidcConfiguration) {
    this.oidcService = oidcService;
    this.authenticationContext = authenticationContext;
    this.consentService = consentService;
    this.oidcConfiguration = oidcConfiguration;
  }

  @Get("/oidc/authorize")
  public HttpResponse<?> authorize(HttpRequest<?> request, @QueryValue("response_type") String responseType,
      @QueryValue("client_id") String clientId,
      @QueryValue("redirect_uri") String redirectUri,
      @QueryValue(value = "scope", defaultValue = "openid") String scope,
      @QueryValue(value = "state", defaultValue = "") String state,
      @QueryValue(value = "nonce", defaultValue = "") String nonce) {
    LOG.info("OIDC authorize request: clientId={}, redirectUri={}, scope={}, statePresent={}, noncePresent={}",
        clientId, redirectUri, scope, state != null && !state.isBlank(), nonce != null && !nonce.isBlank());
    if (!"code".equals(responseType)) {
      LOG.warn("OIDC authorize rejected: unsupported response_type={}", responseType);
      return HttpResponse.badRequest();
    }
    Optional<OidcClient> client = oidcService.resolveClient(clientId);
    if (client.isEmpty() || !client.get().isRedirectUriAllowed(redirectUri)) {
      LOG.warn("OIDC authorize rejected: invalid client or redirectUri. clientId={}, redirectUri={}",
          clientId, redirectUri);
      return HttpResponse.status(HttpStatus.UNAUTHORIZED);
    }
    Optional<Authentication> authentication = authenticationContext.getAuthentication();
    if (authentication.isEmpty()) {
      LOG.info("OIDC authorize unauthenticated, redirecting to login. clientId={}, redirectUri={}",
          clientId, redirectUri);
      URI location = UriBuilder.of(oidcConfiguration.getLoginPageUri())
          .queryParam("redirect", request.getUri().toString())
          .build();
      return temporaryRedirect(location);
    }
    String subject = String.valueOf(authentication.get().getPrincipal());
    if (!oidcService.isUserTenantAllowedForClient(subject, clientId)) {
      LOG.warn("OIDC authorize rejected by tenant check. clientId={}, subject={}", clientId, subject);
      return errorRedirect(redirectUri, "access_denied", state, "tenant_mismatch");
    }
    Set<String> scopes = parseScopes(scope);
    if (!consentService.hasConsent(subject, clientId, scopes)) {
      LOG.info("OIDC authorize consent required. clientId={}, subject={}, scopes={}", clientId, subject, scopes);
      Optional<OidcConsentRequest> pending = consentService.createPendingRequest(request, subject, clientId,
          redirectUri, scopes, state, nonce.isBlank() ? null : nonce);
      if (pending.isEmpty()) {
        LOG.warn("OIDC authorize failed to create pending consent. clientId={}, subject={}", clientId, subject);
        return HttpResponse.status(HttpStatus.UNAUTHORIZED);
      }
      URI consentPage = UriBuilder.of("/oidc/consent")
          .queryParam("request_id", pending.get().getRequestId())
          .build();
      LOG.info("OIDC authorize redirecting to consent page. requestId={}", pending.get().getRequestId());
      return temporaryRedirect(consentPage);
    }
    OidcAuthorizationCode code = oidcService.issueAuthorizationCode(clientId, redirectUri, subject, scopes,
        nonce.isBlank() ? null : nonce);
    LOG.info("OIDC authorize issued code. clientId={}, subject={}, scopes={}", clientId, subject, scopes);
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
