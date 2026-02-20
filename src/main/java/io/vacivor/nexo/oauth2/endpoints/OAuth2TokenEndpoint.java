package io.vacivor.nexo.oauth2.endpoints;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.vacivor.nexo.oidc.OidcAccessToken;
import io.vacivor.nexo.oidc.OidcAuthorizationCode;
import io.vacivor.nexo.oidc.OidcClient;
import io.vacivor.nexo.oidc.OidcRefreshToken;
import io.vacivor.nexo.oidc.OidcService;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Controller
@Requires(property = "nexo.oauth2.enabled", value = "true", defaultValue = "false")
public class OAuth2TokenEndpoint {

  private final OidcService oidcService;

  public OAuth2TokenEndpoint(OidcService oidcService) {
    this.oidcService = oidcService;
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
    Optional<OidcRefreshToken> refreshToken = oidcService.issueRefreshToken(authCode.getSubject(), clientId,
        authCode.getScopes());
    Map<String, Object> response = new HashMap<>();
    response.put("access_token", accessToken.getToken());
    response.put("token_type", "Bearer");
    response.put("expires_in", accessToken.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond());
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
    Optional<OidcRefreshToken> rotated = oidcService.issueRefreshToken(refreshToken.getSubject(), clientId, scopes);
    Map<String, Object> response = new HashMap<>();
    response.put("access_token", accessToken.getToken());
    response.put("token_type", "Bearer");
    response.put("expires_in", accessToken.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond());
    response.put("scope", String.join(" ", scopes));
    rotated.ifPresent(token -> response.put("refresh_token", token.getToken()));
    return HttpResponse.ok(response);
  }

  private Set<String> parseScopes(String scope) {
    if (scope == null || scope.isBlank()) {
      return Collections.emptySet();
    }
    return new HashSet<>(Arrays.asList(scope.trim().split("\\s+")));
  }
}
