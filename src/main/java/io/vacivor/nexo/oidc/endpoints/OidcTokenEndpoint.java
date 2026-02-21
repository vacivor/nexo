package io.vacivor.nexo.oidc.endpoints;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.annotation.Serdeable;
import io.vacivor.nexo.authorizationserver.authorization.AuthorizationService;
import io.vacivor.nexo.authorizationserver.client.AuthorizationClientService;
import io.vacivor.nexo.authorizationserver.client.ClientCredentialsResolver;
import io.vacivor.nexo.authorizationserver.oidc.OidcIdTokenService;
import io.vacivor.nexo.authorizationserver.oidc.OidcTokenService;
import io.vacivor.nexo.oidc.OidcAccessToken;
import io.vacivor.nexo.oidc.OidcAuthorizationCode;
import io.vacivor.nexo.oidc.OidcClient;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Controller
public class OidcTokenEndpoint {

  private final AuthorizationClientService authorizationClientService;
  private final AuthorizationService authorizationService;
  private final OidcTokenService tokenService;
  private final OidcIdTokenService oidcIdTokenService;
  private final ClientCredentialsResolver clientCredentialsResolver;

  public OidcTokenEndpoint(AuthorizationClientService authorizationClientService,
      AuthorizationService authorizationService,
      OidcTokenService tokenService,
      OidcIdTokenService oidcIdTokenService,
      ClientCredentialsResolver clientCredentialsResolver) {
    this.authorizationClientService = authorizationClientService;
    this.authorizationService = authorizationService;
    this.tokenService = tokenService;
    this.oidcIdTokenService = oidcIdTokenService;
    this.clientCredentialsResolver = clientCredentialsResolver;
  }

  @Post(value = "/oidc/token", consumes = MediaType.APPLICATION_FORM_URLENCODED, produces = MediaType.APPLICATION_JSON)
  public HttpResponse<?> token(HttpRequest<?> request, @Body Map<String, String> body) {
    String grantType = body.get("grant_type");
    if (!"authorization_code".equals(grantType)) {
      return oauthError(HttpStatus.BAD_REQUEST, "unsupported_grant_type");
    }
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
    OidcAccessToken accessToken = tokenService.issueAccessToken(authCode.getSubject(), clientId,
        authCode.getScopes());
    String idToken = oidcIdTokenService.issueIdToken(authCode.getSubject(), clientId, authCode.getNonce());

    return HttpResponse.ok(new OidcTokenResponse(
        accessToken.getToken(),
        "Bearer",
        accessToken.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond(),
        idToken,
        String.join(" ", authCode.getScopes())));
  }

  private HttpResponse<OAuthErrorResponse> oauthError(HttpStatus status, String errorCode) {
    return HttpResponse.status(status).body(new OAuthErrorResponse(errorCode));
  }

  @Serdeable
  private record OidcTokenResponse(String access_token, String token_type, long expires_in, String id_token,
                                   String scope) {
  }

  @Serdeable
  private record OAuthErrorResponse(String error) {
  }
}
