package io.vacivor.nexo.oauth2.endpoints;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.vacivor.nexo.authorizationserver.authorization.AuthorizationService;
import io.vacivor.nexo.authorizationserver.client.AuthorizationClientService;
import io.vacivor.nexo.authorizationserver.oauth2.OAuth2TokenService;
import io.vacivor.nexo.core.RefreshTokenConsumeResult;
import io.vacivor.nexo.core.RefreshTokenConsumeStatus;
import io.vacivor.nexo.oidc.OidcAuthorizationCode;
import io.vacivor.nexo.oidc.OidcClient;
import io.vacivor.nexo.oauth2.token.OAuth2AccessToken;
import io.vacivor.nexo.oauth2.token.OAuth2RefreshToken;
import java.time.Instant;
import java.util.Base64;
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

  private final AuthorizationClientService authorizationClientService;
  private final AuthorizationService authorizationService;
  private final OAuth2TokenService tokenService;

  public OAuth2TokenEndpoint(AuthorizationClientService authorizationClientService,
      AuthorizationService authorizationService,
      OAuth2TokenService tokenService) {
    this.authorizationClientService = authorizationClientService;
    this.authorizationService = authorizationService;
    this.tokenService = tokenService;
  }

  @Post(uri = "/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED, produces = MediaType.APPLICATION_JSON)
  public HttpResponse<?> token(HttpRequest<?> request, @Body Map<String, String> body) {
    String grantType = body.get("grant_type");
    if ("authorization_code".equals(grantType)) {
      return exchangeAuthorizationCode(request, body);
    }
    if ("client_credentials".equals(grantType)) {
      return exchangeClientCredentials(request, body);
    }
    if ("refresh_token".equals(grantType)) {
      return exchangeRefreshToken(request, body);
    }
    return oauthError(HttpStatus.BAD_REQUEST, "unsupported_grant_type");
  }

  private HttpResponse<?> exchangeAuthorizationCode(HttpRequest<?> request, Map<String, String> body) {
    String code = body.get("code");
    String redirectUri = body.get("redirect_uri");
    String clientId = trimToNull(body.get("client_id"));
    String clientSecret = trimToNull(body.get("client_secret"));
    if (clientId == null || clientSecret == null) {
      Map<String, String> basicAuth = resolveBasicClientCredentials(request);
      clientId = clientId != null ? clientId : basicAuth.get("client_id");
      clientSecret = clientSecret != null ? clientSecret : basicAuth.get("client_secret");
    }
    if (code == null || clientId == null || redirectUri == null) {
      return oauthError(HttpStatus.BAD_REQUEST, "invalid_request");
    }
    Optional<OidcClient> client = authorizationClientService.resolveClient(clientId);
    if (client.isEmpty() || !client.get().verifySecret(clientSecret)
        || !client.get().isRedirectUriAllowed(redirectUri)) {
      return oauthError(HttpStatus.UNAUTHORIZED, "invalid_client");
    }
    Optional<OidcAuthorizationCode> stored = authorizationService.consumeCode(code);
    if (stored.isEmpty()) {
      return oauthError(HttpStatus.BAD_REQUEST, "invalid_grant");
    }
    OidcAuthorizationCode authCode = stored.get();
    if (!clientId.equals(authCode.getClientId()) || !redirectUri.equals(authCode.getRedirectUri())) {
      return oauthError(HttpStatus.BAD_REQUEST, "invalid_grant");
    }
    Optional<OAuth2RefreshToken> refreshToken = tokenService.issueRefreshToken(
        authCode.getSubject(), clientId, authCode.getScopes());
    String familyId = refreshToken.map(OAuth2RefreshToken::getFamilyId).orElse(null);
    OAuth2AccessToken accessToken = tokenService.issueAccessToken(authCode.getSubject(), clientId,
        authCode.getScopes(), familyId);
    Map<String, Object> response = new HashMap<>();
    response.put("access_token", accessToken.getToken());
    response.put("token_type", "Bearer");
    response.put("expires_in", accessToken.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond());
    response.put("scope", String.join(" ", authCode.getScopes()));
    refreshToken.ifPresent(token -> response.put("refresh_token", token.getToken()));
    return HttpResponse.ok(response);
  }

  private HttpResponse<?> exchangeClientCredentials(HttpRequest<?> request, Map<String, String> body) {
    String clientId = trimToNull(body.get("client_id"));
    String clientSecret = trimToNull(body.get("client_secret"));
    if (clientId == null || clientSecret == null) {
      Map<String, String> basicAuth = resolveBasicClientCredentials(request);
      clientId = clientId != null ? clientId : basicAuth.get("client_id");
      clientSecret = clientSecret != null ? clientSecret : basicAuth.get("client_secret");
    }
    String scope = body.getOrDefault("scope", "");
    if (clientId == null) {
      return oauthError(HttpStatus.BAD_REQUEST, "invalid_request");
    }
    Optional<OidcClient> client = authorizationClientService.resolveClient(clientId);
    if (client.isEmpty() || (client.get().isConfidential() && !client.get().verifySecret(clientSecret))) {
      return oauthError(HttpStatus.UNAUTHORIZED, "invalid_client");
    }
    Set<String> scopes = parseScopes(scope);
    OAuth2AccessToken accessToken = tokenService.issueAccessToken(clientId, clientId, scopes);
    Map<String, Object> response = new HashMap<>();
    response.put("access_token", accessToken.getToken());
    response.put("token_type", "Bearer");
    response.put("expires_in", accessToken.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond());
    if (!scopes.isEmpty()) {
      response.put("scope", String.join(" ", scopes));
    }
    return HttpResponse.ok(response);
  }

  private HttpResponse<?> exchangeRefreshToken(HttpRequest<?> request, Map<String, String> body) {
    String refreshTokenValue = body.get("refresh_token");
    String clientId = trimToNull(body.get("client_id"));
    String clientSecret = trimToNull(body.get("client_secret"));
    if (clientId == null || clientSecret == null) {
      Map<String, String> basicAuth = resolveBasicClientCredentials(request);
      clientId = clientId != null ? clientId : basicAuth.get("client_id");
      clientSecret = clientSecret != null ? clientSecret : basicAuth.get("client_secret");
    }
    String scope = body.getOrDefault("scope", "");
    if (refreshTokenValue == null || clientId == null) {
      return oauthError(HttpStatus.BAD_REQUEST, "invalid_request");
    }
    Optional<OidcClient> client = authorizationClientService.resolveClient(clientId);
    if (client.isEmpty() || (client.get().isConfidential() && !client.get().verifySecret(clientSecret))) {
      return oauthError(HttpStatus.UNAUTHORIZED, "invalid_client");
    }
    RefreshTokenConsumeResult<OAuth2RefreshToken> consumeResult = tokenService.consumeRefreshTokenSecure(
        refreshTokenValue);
    if (consumeResult.getStatus() == RefreshTokenConsumeStatus.REUSED) {
      return oauthError(HttpStatus.BAD_REQUEST, "invalid_grant", "refresh_token_reuse_detected");
    }
    if (consumeResult.getStatus() != RefreshTokenConsumeStatus.CONSUMED
        || consumeResult.getToken().isEmpty()) {
      return oauthError(HttpStatus.BAD_REQUEST, "invalid_grant");
    }
    OAuth2RefreshToken refreshToken = consumeResult.getToken().get();
    if (!clientId.equals(refreshToken.getClientId())) {
      return oauthError(HttpStatus.BAD_REQUEST, "invalid_grant");
    }
    Set<String> scopes = tokenService.resolveRefreshScopes(refreshToken, scope);
    if (scopes.isEmpty() && scope != null && !scope.isBlank()) {
      return oauthError(HttpStatus.BAD_REQUEST, "invalid_scope");
    }
    OAuth2AccessToken accessToken = tokenService.issueAccessToken(
        refreshToken.getSubject(), clientId, scopes, refreshToken.getFamilyId());
    Optional<OAuth2RefreshToken> rotated = tokenService.issueRefreshToken(
        refreshToken.getSubject(), clientId, scopes, refreshToken.getFamilyId());
    Map<String, Object> response = new HashMap<>();
    response.put("access_token", accessToken.getToken());
    response.put("token_type", "Bearer");
    response.put("expires_in", accessToken.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond());
    response.put("scope", String.join(" ", scopes));
    rotated.ifPresent(token -> response.put("refresh_token", token.getToken()));
    return HttpResponse.ok(response);
  }

  private HttpResponse<Map<String, Object>> oauthError(HttpStatus status, String errorCode) {
    return oauthError(status, errorCode, null);
  }

  private HttpResponse<Map<String, Object>> oauthError(HttpStatus status, String errorCode, String description) {
    Map<String, Object> body = new HashMap<>();
    body.put("error", errorCode);
    if (description != null && !description.isBlank()) {
      body.put("error_description", description);
    }
    return HttpResponse.status(status).body(body);
  }

  private Set<String> parseScopes(String scope) {
    if (scope == null || scope.isBlank()) {
      return Collections.emptySet();
    }
    return new HashSet<>(Arrays.asList(scope.trim().split("\\s+")));
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private Map<String, String> resolveBasicClientCredentials(HttpRequest<?> request) {
    String authorization = request.getHeaders().get("Authorization");
    if (authorization == null || !authorization.startsWith("Basic ")) {
      return Map.of();
    }
    try {
      String base64Part = authorization.substring("Basic ".length()).trim();
      String decoded = new String(Base64.getDecoder().decode(base64Part));
      int separator = decoded.indexOf(':');
      if (separator <= 0) {
        return Map.of();
      }
      String clientId = decoded.substring(0, separator);
      String clientSecret = decoded.substring(separator + 1);
      if (clientId.isBlank() || clientSecret.isBlank()) {
        return Map.of();
      }
      return Map.of("client_id", clientId, "client_secret", clientSecret);
    } catch (IllegalArgumentException e) {
      return Map.of();
    }
  }
}
