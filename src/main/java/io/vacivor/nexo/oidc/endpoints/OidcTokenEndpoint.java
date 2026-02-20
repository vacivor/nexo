package io.vacivor.nexo.oidc.endpoints;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.vacivor.nexo.oidc.OidcAccessToken;
import io.vacivor.nexo.oidc.OidcAuthorizationCode;
import io.vacivor.nexo.oidc.OidcClient;
import io.vacivor.nexo.oidc.OidcService;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class OidcTokenEndpoint {

  private final OidcService oidcService;

  public OidcTokenEndpoint(OidcService oidcService) {
    this.oidcService = oidcService;
  }

  @Post(value = "/oidc/token", consumes = MediaType.APPLICATION_FORM_URLENCODED, produces = MediaType.APPLICATION_JSON)
  public HttpResponse<?> token(HttpRequest<?> request, @Body Map<String, String> body) {
    String grantType = body.get("grant_type");
    if (!"authorization_code".equals(grantType)) {
      return oauthError(HttpStatus.BAD_REQUEST, "unsupported_grant_type");
    }
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
    Optional<OidcClient> client = oidcService.resolveClient(clientId);
    if (client.isEmpty() || !client.get().verifySecret(clientSecret)
        || !client.get().isRedirectUriAllowed(redirectUri)) {
      return oauthError(HttpStatus.UNAUTHORIZED, "invalid_client");
    }
    Optional<OidcAuthorizationCode> stored = oidcService.consumeCode(code);
    if (stored.isEmpty()) {
      return oauthError(HttpStatus.BAD_REQUEST, "invalid_grant");
    }
    OidcAuthorizationCode authCode = stored.get();
    if (!clientId.equals(authCode.getClientId()) || !redirectUri.equals(authCode.getRedirectUri())) {
      return oauthError(HttpStatus.BAD_REQUEST, "invalid_grant");
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
