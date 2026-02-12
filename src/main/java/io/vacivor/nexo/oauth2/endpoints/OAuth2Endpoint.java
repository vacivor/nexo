package io.vacivor.nexo.oauth2.endpoints;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.uri.UriBuilder;
import io.vacivor.nexo.security.auth.Authentication;
import io.vacivor.nexo.security.auth.AuthenticationContext;
import io.vacivor.nexo.security.oidc.OidcAccessToken;
import io.vacivor.nexo.security.oidc.OidcAuthorizationCode;
import io.vacivor.nexo.security.oidc.OidcClient;
import io.vacivor.nexo.security.oidc.OidcService;
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
public class OAuth2Endpoint {

  private final OidcService oidcService;
  private final AuthenticationContext authenticationContext;

  public OAuth2Endpoint(OidcService oidcService, AuthenticationContext authenticationContext) {
    this.oidcService = oidcService;
    this.authenticationContext = authenticationContext;
  }

  @Get(uri = "/oauth/authorize")
  public HttpResponse<?> authorize(@QueryValue("response_type") String responseType,
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
    OidcAuthorizationCode code = oidcService.issueAuthorizationCode(clientId, redirectUri, subject, scopes,
        nonce.isBlank() ? null : nonce);
    URI location = UriBuilder.of(redirectUri)
        .queryParam("code", code.getCode())
        .queryParam("state", state)
        .build();
    return HttpResponse.redirect(location);
  }

  @Post(uri = "/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED, produces = MediaType.APPLICATION_JSON)
  public HttpResponse<?> token(@Body Map<String, String> body) {
    String grantType = body.get("grant_type");
    if (!"authorization_code".equals(grantType)) {
      return HttpResponse.badRequest();
    }
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

    Map<String, Object> response = new HashMap<>();
    response.put("access_token", accessToken.getToken());
    response.put("token_type", "Bearer");
    response.put("expires_in", accessToken.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond());
    response.put("id_token", idToken);
    response.put("scope", String.join(" ", authCode.getScopes()));
    return HttpResponse.ok(response);
  }

  private Set<String> parseScopes(String scope) {
    if (scope == null || scope.isBlank()) {
      return Collections.emptySet();
    }
    return new HashSet<>(Arrays.asList(scope.trim().split("\\s+")));
  }
}
