package io.vacivor.nexo.oauth2.endpoints;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.vacivor.nexo.authorizationserver.client.AuthorizationClientService;
import io.vacivor.nexo.authorizationserver.oauth2.OAuth2TokenService;
import io.vacivor.nexo.oidc.OidcClient;
import io.vacivor.nexo.oauth2.token.OAuth2AccessToken;
import io.vacivor.nexo.oauth2.token.OAuth2RefreshToken;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@Requires(property = "nexo.oauth2.enabled", value = "true", defaultValue = "false")
public class OAuth2RevocationEndpoint {

  private final AuthorizationClientService authorizationClientService;
  private final OAuth2TokenService tokenService;

  public OAuth2RevocationEndpoint(AuthorizationClientService authorizationClientService,
      OAuth2TokenService tokenService) {
    this.authorizationClientService = authorizationClientService;
    this.tokenService = tokenService;
  }

  @Post(uri = "/oauth/revoke", consumes = MediaType.APPLICATION_FORM_URLENCODED, produces = MediaType.APPLICATION_JSON)
  public HttpResponse<?> revoke(HttpRequest<?> request, @Body Map<String, String> body) {
    String token = trimToNull(body.get("token"));
    String tokenTypeHint = trimToNull(body.get("token_type_hint"));
    String clientId = trimToNull(body.get("client_id"));
    String clientSecret = trimToNull(body.get("client_secret"));
    if (clientId == null || clientSecret == null) {
      Map<String, String> basicAuth = resolveBasicClientCredentials(request);
      clientId = clientId != null ? clientId : basicAuth.get("client_id");
      clientSecret = clientSecret != null ? clientSecret : basicAuth.get("client_secret");
    }
    if (token == null || clientId == null) {
      return oauthError(HttpStatus.BAD_REQUEST, "invalid_request");
    }
    Optional<OidcClient> client = authorizationClientService.resolveClient(clientId);
    if (client.isEmpty() || (client.get().isConfidential() && !client.get().verifySecret(clientSecret))) {
      return oauthError(HttpStatus.UNAUTHORIZED, "invalid_client");
    }
    revokeInternal(token, tokenTypeHint, clientId);
    return HttpResponse.ok(Map.of());
  }

  private void revokeInternal(String token, String tokenTypeHint, String clientId) {
    if ("access_token".equals(tokenTypeHint)) {
      tokenService.findAccessToken(token)
          .filter(accessToken -> clientId.equals(accessToken.getClientId()))
          .ifPresent(accessToken -> tokenService.revokeAccessToken(accessToken.getToken()));
      return;
    }
    if ("refresh_token".equals(tokenTypeHint)) {
      tokenService.findRefreshToken(token)
          .filter(refreshToken -> clientId.equals(refreshToken.getClientId()))
          .ifPresent(refreshToken -> tokenService.revokeRefreshToken(refreshToken.getToken()));
      return;
    }
    Optional<OAuth2RefreshToken> refreshToken = tokenService.findRefreshToken(token)
        .filter(found -> clientId.equals(found.getClientId()));
    if (refreshToken.isPresent()) {
      tokenService.revokeRefreshToken(refreshToken.get().getToken());
      return;
    }
    Optional<OAuth2AccessToken> accessToken = tokenService.findAccessToken(token)
        .filter(found -> clientId.equals(found.getClientId()));
    accessToken.ifPresent(found -> tokenService.revokeAccessToken(found.getToken()));
  }

  private HttpResponse<Map<String, Object>> oauthError(HttpStatus status, String errorCode) {
    Map<String, Object> body = new HashMap<>();
    body.put("error", errorCode);
    return HttpResponse.status(status).body(body);
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
