package io.vacivor.nexo.oidc.endpoints;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.uri.UriBuilder;
import io.vacivor.nexo.oidc.OidcAccessToken;
import io.vacivor.nexo.oidc.OidcAuthorizeResponse;
import io.vacivor.nexo.oidc.OidcAuthorizationCode;
import io.vacivor.nexo.oidc.OidcClient;
import io.vacivor.nexo.oidc.OidcConsentDecisionRequest;
import io.vacivor.nexo.oidc.OidcConsentRequest;
import io.vacivor.nexo.oidc.OidcConsentService;
import io.vacivor.nexo.oidc.OidcRefreshToken;
import io.vacivor.nexo.oidc.OidcService;
import io.vacivor.nexo.security.auth.Authentication;
import io.vacivor.nexo.security.auth.AuthenticationContext;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Controller
@Requires(property = "nexo.oidc.enabled", value = "true", defaultValue = "false")
public class OidcEndpoint {

  private final OidcService oidcService;
  private final OidcConsentService consentService;
  private final AuthenticationContext authenticationContext;

  public OidcEndpoint(OidcService oidcService, OidcConsentService consentService,
      AuthenticationContext authenticationContext) {
    this.oidcService = oidcService;
    this.consentService = consentService;
    this.authenticationContext = authenticationContext;
  }

  @Get(uri = "/oauth/authorize")
  public HttpResponse<?> authorize(HttpRequest<?> request,
      @QueryValue("response_type") String responseType,
      @QueryValue("client_id") String clientId,
      @QueryValue("redirect_uri") String redirectUri,
      @QueryValue(value = "scope", defaultValue = "openid") String scope,
      @QueryValue(value = "state", defaultValue = "") String state,
      @QueryValue(value = "nonce", defaultValue = "") String nonce) {
    if (!"code".equals(responseType)) {
      return errorRedirect(redirectUri, "unsupported_response_type", state);
    }
    Optional<OidcClient> client = oidcService.resolveClient(clientId);
    if (client.isEmpty() || !client.get().isRedirectUriAllowed(redirectUri)) {
      return HttpResponse.status(HttpStatus.UNAUTHORIZED);
    }
    Set<String> scopes = parseScopes(scope);
    if (!scopes.contains("openid")) {
      return errorRedirect(redirectUri, "invalid_scope", state);
    }
    Optional<Authentication> authentication = authenticationContext.getAuthentication();
    if (authentication.isEmpty()) {
      return HttpResponse.status(HttpStatus.UNAUTHORIZED);
    }
    String subject = String.valueOf(authentication.get().getPrincipal());

    if (!consentService.hasConsent(subject, clientId, scopes)) {
      Optional<OidcConsentRequest> pending = consentService.createPendingRequest(request, subject,
          clientId, redirectUri, scopes, state, nonce.isBlank() ? null : nonce);
      if (pending.isEmpty()) {
        return HttpResponse.status(HttpStatus.UNAUTHORIZED);
      }
      OidcConsentRequest consentRequest = pending.get();
      return HttpResponse.ok(new OidcAuthorizeResponse(consentRequest.getRequestId(),
          consentRequest.getCsrfToken(), clientId, scopes, state));
    }

    OidcAuthorizationCode code = oidcService.issueAuthorizationCode(clientId, redirectUri, subject,
        scopes, nonce.isBlank() ? null : nonce);
    URI location = UriBuilder.of(redirectUri)
        .queryParam("code", code.getCode())
        .queryParam("state", state)
        .build();
    return HttpResponse.redirect(location);
  }

  @Post(uri = "/oauth/consent", consumes = MediaType.APPLICATION_JSON)
  public HttpResponse<?> consent(HttpRequest<?> request, @Body OidcConsentDecisionRequest decision) {
    if (decision == null || decision.getRequestId() == null || decision.getCsrfToken() == null) {
      return HttpResponse.badRequest();
    }
    Optional<OidcConsentRequest> pending = consentService.findPendingRequest(request,
        decision.getRequestId());
    if (pending.isEmpty()) {
      return HttpResponse.status(HttpStatus.BAD_REQUEST);
    }
    OidcConsentRequest consentRequest = pending.get();
    if (!decision.getCsrfToken().equals(consentRequest.getCsrfToken())) {
      return HttpResponse.status(HttpStatus.BAD_REQUEST);
    }
    if (!decision.isApprove()) {
      return errorRedirect(consentRequest.getRedirectUri(), "access_denied", consentRequest.getState());
    }
    consentService.approveConsent(consentRequest.getSubject(), consentRequest.getClientId(),
        consentRequest.getScopes());
    OidcAuthorizationCode code = oidcService.issueAuthorizationCode(consentRequest.getClientId(),
        consentRequest.getRedirectUri(), consentRequest.getSubject(), consentRequest.getScopes(),
        consentRequest.getNonce());
    URI location = UriBuilder.of(consentRequest.getRedirectUri())
        .queryParam("code", code.getCode())
        .queryParam("state", consentRequest.getState())
        .build();
    return HttpResponse.redirect(location);
  }

  @Post(uri = "/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED, produces = MediaType.APPLICATION_JSON)
  public HttpResponse<?> token(@Body Map<String, String> body) {
    String grantType = body.get("grant_type");
    if ("authorization_code".equals(grantType)) {
      return exchangeAuthorizationCode(body);
    }
    if ("client_credentials".equals(grantType)) {
      return exchangeClientCredentials(body);
    }
    if ("refresh_token".equals(grantType)) {
      return exchangeRefreshToken(body);
    }
    return HttpResponse.badRequest();
  }

  private HttpResponse<?> exchangeAuthorizationCode(Map<String, String> body) {
    String code = body.get("code");
    String redirectUri = body.get("redirect_uri");
    String clientId = body.get("client_id");
    String clientSecret = body.get("client_secret");
    if (code == null || clientId == null || redirectUri == null) {
      return HttpResponse.badRequest();
    }
    Optional<OidcClient> client = oidcService.resolveClient(clientId);
    if (client.isEmpty() || !client.get().verifySecret(clientSecret)
        || !client.get().isRedirectUriAllowed(redirectUri)) {
      return HttpResponse.status(HttpStatus.UNAUTHORIZED);
    }
    Optional<OidcAuthorizationCode> stored = oidcService.consumeCode(code);
    if (stored.isEmpty()) {
      return HttpResponse.status(HttpStatus.BAD_REQUEST);
    }
    OidcAuthorizationCode authCode = stored.get();
    if (!clientId.equals(authCode.getClientId()) || !redirectUri.equals(authCode.getRedirectUri())) {
      return HttpResponse.status(HttpStatus.BAD_REQUEST);
    }
    OidcAccessToken accessToken = oidcService.issueAccessToken(authCode.getSubject(), clientId,
        authCode.getScopes());
    String idToken = oidcService.issueIdToken(authCode.getSubject(), clientId, authCode.getNonce());
    Optional<OidcRefreshToken> refreshToken = oidcService.issueRefreshToken(authCode.getSubject(), clientId,
        authCode.getScopes());

    Map<String, Object> response = new HashMap<>();
    response.put("access_token", accessToken.getToken());
    response.put("token_type", "Bearer");
    response.put("expires_in", accessToken.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond());
    response.put("id_token", idToken);
    response.put("scope", String.join(" ", authCode.getScopes()));
    refreshToken.ifPresent(token -> response.put("refresh_token", token.getToken()));
    return HttpResponse.ok(response);
  }

  private HttpResponse<?> exchangeClientCredentials(Map<String, String> body) {
    String clientId = body.get("client_id");
    String clientSecret = body.get("client_secret");
    String scope = body.getOrDefault("scope", "");
    if (clientId == null) {
      return HttpResponse.badRequest();
    }
    Optional<OidcClient> client = oidcService.resolveClient(clientId);
    if (client.isEmpty() || (client.get().isConfidential() && !client.get().verifySecret(clientSecret))) {
      return HttpResponse.status(HttpStatus.UNAUTHORIZED);
    }
    Set<String> scopes = parseScopes(scope);
    if (scopes.contains("openid")) {
      return HttpResponse.badRequest();
    }
    OidcAccessToken accessToken = oidcService.issueAccessToken(clientId, clientId, scopes);
    Map<String, Object> response = new HashMap<>();
    response.put("access_token", accessToken.getToken());
    response.put("token_type", "Bearer");
    response.put("expires_in", accessToken.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond());
    if (!scopes.isEmpty()) {
      response.put("scope", String.join(" ", scopes));
    }
    return HttpResponse.ok(response);
  }

  private HttpResponse<?> exchangeRefreshToken(Map<String, String> body) {
    String refreshTokenValue = body.get("refresh_token");
    String clientId = body.get("client_id");
    String clientSecret = body.get("client_secret");
    String scope = body.getOrDefault("scope", "");
    if (refreshTokenValue == null || clientId == null) {
      return HttpResponse.badRequest();
    }
    Optional<OidcClient> client = oidcService.resolveClient(clientId);
    if (client.isEmpty() || (client.get().isConfidential() && !client.get().verifySecret(clientSecret))) {
      return HttpResponse.status(HttpStatus.UNAUTHORIZED);
    }
    Optional<OidcRefreshToken> stored = oidcService.consumeRefreshToken(refreshTokenValue);
    if (stored.isEmpty()) {
      return HttpResponse.status(HttpStatus.BAD_REQUEST);
    }
    OidcRefreshToken refreshToken = stored.get();
    if (!clientId.equals(refreshToken.getClientId())) {
      return HttpResponse.status(HttpStatus.BAD_REQUEST);
    }
    Set<String> scopes = scope == null || scope.isBlank() ? refreshToken.getScopes() : parseScopes(scope);
    OidcAccessToken accessToken = oidcService.issueAccessToken(refreshToken.getSubject(), clientId, scopes);
    String idToken = scopes.contains("openid")
        ? oidcService.issueIdToken(refreshToken.getSubject(), clientId, null)
        : null;
    Optional<OidcRefreshToken> rotated = oidcService.issueRefreshToken(refreshToken.getSubject(), clientId, scopes);
    Map<String, Object> response = new HashMap<>();
    response.put("access_token", accessToken.getToken());
    response.put("token_type", "Bearer");
    response.put("expires_in", accessToken.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond());
    response.put("scope", String.join(" ", scopes));
    if (idToken != null) {
      response.put("id_token", idToken);
    }
    rotated.ifPresent(token -> response.put("refresh_token", token.getToken()));
    return HttpResponse.ok(response);
  }

  private Set<String> parseScopes(String scope) {
    if (scope == null || scope.isBlank()) {
      return Collections.emptySet();
    }
    return new HashSet<>(Arrays.asList(scope.trim().split("\\s+")));
  }

  private HttpResponse<?> errorRedirect(String redirectUri, String error, String state) {
    if (redirectUri == null || redirectUri.isBlank()) {
      return HttpResponse.status(HttpStatus.BAD_REQUEST);
    }
    URI location = UriBuilder.of(redirectUri)
        .queryParam("error", error)
        .queryParam("state", state)
        .build();
    return HttpResponse.redirect(location);
  }
}
