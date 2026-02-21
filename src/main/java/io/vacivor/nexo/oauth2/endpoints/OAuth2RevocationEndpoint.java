package io.vacivor.nexo.oauth2.endpoints;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.annotation.Serdeable;
import io.vacivor.nexo.authorizationserver.client.AuthorizationClientService;
import io.vacivor.nexo.authorizationserver.client.ClientCredentialsResolver;
import io.vacivor.nexo.authorizationserver.oauth2.OAuth2TokenService;
import io.vacivor.nexo.oidc.OidcClient;
import io.vacivor.nexo.oauth2.token.OAuth2AccessToken;
import io.vacivor.nexo.oauth2.token.OAuth2RefreshToken;
import java.util.Map;
import java.util.Optional;

@Controller
@Requires(property = "nexo.oauth2.enabled", value = "true", defaultValue = "false")
public class OAuth2RevocationEndpoint {

  private final AuthorizationClientService authorizationClientService;
  private final OAuth2TokenService tokenService;
  private final ClientCredentialsResolver clientCredentialsResolver;

  public OAuth2RevocationEndpoint(AuthorizationClientService authorizationClientService,
      OAuth2TokenService tokenService,
      ClientCredentialsResolver clientCredentialsResolver) {
    this.authorizationClientService = authorizationClientService;
    this.tokenService = tokenService;
    this.clientCredentialsResolver = clientCredentialsResolver;
  }

  @Post(uri = "/oauth/revoke", consumes = MediaType.APPLICATION_FORM_URLENCODED, produces = MediaType.APPLICATION_JSON)
  public HttpResponse<?> revoke(HttpRequest<?> request, @Body Map<String, String> body) {
    String token = trimToNull(body.get("token"));
    String tokenTypeHint = trimToNull(body.get("token_type_hint"));
    Optional<ClientCredentialsResolver.ClientCredentials> credentials = clientCredentialsResolver.resolve(request, body);
    String clientId = credentials.map(ClientCredentialsResolver.ClientCredentials::clientId).orElse(null);
    String clientSecret = credentials.map(ClientCredentialsResolver.ClientCredentials::clientSecret).orElse(null);
    if (token == null || clientId == null) {
      return oauthError(HttpStatus.BAD_REQUEST, "invalid_request");
    }
    Optional<OidcClient> client = authorizationClientService.resolveClient(clientId);
    if (client.isEmpty() || (client.get().isConfidential() && !client.get().verifySecret(clientSecret))) {
      return oauthError(HttpStatus.UNAUTHORIZED, "invalid_client");
    }
    revokeInternal(token, tokenTypeHint, clientId);
    return HttpResponse.ok(new RevocationResponse(true));
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

  private HttpResponse<OAuthErrorResponse> oauthError(HttpStatus status, String errorCode) {
    return HttpResponse.status(status).body(new OAuthErrorResponse(errorCode));
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  @Serdeable
  private record OAuthErrorResponse(String error) {
  }

  @Serdeable
  private record RevocationResponse(boolean revoked) {
  }
}
