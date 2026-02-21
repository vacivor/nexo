package io.vacivor.nexo.oauth2.endpoints;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.annotation.Serdeable;
import io.vacivor.nexo.authorizationserver.authorization.AuthorizationService;
import io.vacivor.nexo.authorizationserver.client.AuthorizationClientService;
import io.vacivor.nexo.authorizationserver.client.ClientCredentialsResolver;
import io.vacivor.nexo.authorizationserver.oauth2.OAuth2TokenService;
import io.vacivor.nexo.core.RefreshTokenConsumeResult;
import io.vacivor.nexo.core.RefreshTokenConsumeStatus;
import io.vacivor.nexo.oidc.OidcAuthorizationCode;
import io.vacivor.nexo.oidc.OidcClient;
import io.vacivor.nexo.oauth2.token.OAuth2AccessToken;
import io.vacivor.nexo.oauth2.token.OAuth2RefreshToken;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

@Controller
@Requires(property = "nexo.oauth2.enabled", value = "true", defaultValue = "false")
public class OAuth2TokenEndpoint {

  private final AuthorizationClientService authorizationClientService;
  private final AuthorizationService authorizationService;
  private final OAuth2TokenService tokenService;
  private final ClientCredentialsResolver clientCredentialsResolver;

  public OAuth2TokenEndpoint(AuthorizationClientService authorizationClientService,
      AuthorizationService authorizationService,
      OAuth2TokenService tokenService,
      ClientCredentialsResolver clientCredentialsResolver) {
    this.authorizationClientService = authorizationClientService;
    this.authorizationService = authorizationService;
    this.tokenService = tokenService;
    this.clientCredentialsResolver = clientCredentialsResolver;
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
    Optional<ClientCredentialsResolver.ClientCredentials> credentials = clientCredentialsResolver.resolve(request, body);
    String clientId = credentials.map(ClientCredentialsResolver.ClientCredentials::clientId).orElse(null);
    String clientSecret = credentials.map(ClientCredentialsResolver.ClientCredentials::clientSecret).orElse(null);
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
    return HttpResponse.ok(new TokenResponse(
        accessToken.getToken(),
        "Bearer",
        accessToken.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond(),
        String.join(" ", authCode.getScopes()),
        refreshToken.map(OAuth2RefreshToken::getToken).orElse(null)));
  }

  private HttpResponse<?> exchangeClientCredentials(HttpRequest<?> request, Map<String, String> body) {
    Optional<ClientCredentialsResolver.ClientCredentials> credentials = clientCredentialsResolver.resolve(request, body);
    String clientId = credentials.map(ClientCredentialsResolver.ClientCredentials::clientId).orElse(null);
    String clientSecret = credentials.map(ClientCredentialsResolver.ClientCredentials::clientSecret).orElse(null);
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
    return HttpResponse.ok(new TokenResponse(
        accessToken.getToken(),
        "Bearer",
        accessToken.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond(),
        scopes.isEmpty() ? null : String.join(" ", scopes),
        null));
  }

  private HttpResponse<?> exchangeRefreshToken(HttpRequest<?> request, Map<String, String> body) {
    String refreshTokenValue = body.get("refresh_token");
    Optional<ClientCredentialsResolver.ClientCredentials> credentials = clientCredentialsResolver.resolve(request, body);
    String clientId = credentials.map(ClientCredentialsResolver.ClientCredentials::clientId).orElse(null);
    String clientSecret = credentials.map(ClientCredentialsResolver.ClientCredentials::clientSecret).orElse(null);
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
    return HttpResponse.ok(new TokenResponse(
        accessToken.getToken(),
        "Bearer",
        accessToken.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond(),
        String.join(" ", scopes),
        rotated.map(OAuth2RefreshToken::getToken).orElse(null)));
  }

  private HttpResponse<OAuthErrorResponse> oauthError(HttpStatus status, String errorCode) {
    return oauthError(status, errorCode, null);
  }

  private HttpResponse<OAuthErrorResponse> oauthError(HttpStatus status, String errorCode, String description) {
    return HttpResponse.status(status).body(new OAuthErrorResponse(errorCode, description));
  }

  private Set<String> parseScopes(String scope) {
    if (scope == null || scope.isBlank()) {
      return Collections.emptySet();
    }
    return new HashSet<>(Arrays.asList(scope.trim().split("\\s+")));
  }

  @Serdeable
  private record TokenResponse(String access_token, String token_type, long expires_in, String scope,
                               String refresh_token) {
  }

  @Serdeable
  private record OAuthErrorResponse(String error, String error_description) {
  }
}
