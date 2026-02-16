package io.vacivor.nexo.oidc.endpoints;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.vacivor.nexo.oidc.OidcAccessToken;
import io.vacivor.nexo.oidc.OidcAuthorizationCode;
import io.vacivor.nexo.oidc.OidcClient;
import io.vacivor.nexo.oidc.OidcService;
import java.time.Instant;
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
}
