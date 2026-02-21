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
import io.vacivor.nexo.authorizationserver.oidc.OidcTokenService;
import io.vacivor.nexo.oauth2.token.OAuth2AccessToken;
import io.vacivor.nexo.oauth2.token.OAuth2RefreshToken;
import io.vacivor.nexo.oidc.OidcAccessToken;
import io.vacivor.nexo.oidc.OidcClient;
import io.vacivor.nexo.oidc.OidcRefreshToken;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Controller
@Requires(property = "nexo.oauth2.enabled", value = "true", defaultValue = "false")
public class OAuth2IntrospectionEndpoint {

  private final AuthorizationClientService authorizationClientService;
  private final ClientCredentialsResolver clientCredentialsResolver;
  private final OAuth2TokenService oAuth2TokenService;
  private final OidcTokenService oidcTokenService;

  public OAuth2IntrospectionEndpoint(AuthorizationClientService authorizationClientService,
      ClientCredentialsResolver clientCredentialsResolver,
      OAuth2TokenService oAuth2TokenService,
      OidcTokenService oidcTokenService) {
    this.authorizationClientService = authorizationClientService;
    this.clientCredentialsResolver = clientCredentialsResolver;
    this.oAuth2TokenService = oAuth2TokenService;
    this.oidcTokenService = oidcTokenService;
  }

  @Post(uri = "/oauth/introspect", consumes = MediaType.APPLICATION_FORM_URLENCODED, produces = MediaType.APPLICATION_JSON)
  public HttpResponse<?> introspect(HttpRequest<?> request, @Body Map<String, String> body) {
    String token = trimToNull(body.get("token"));
    String tokenTypeHint = trimToNull(body.get("token_type_hint"));
    if (token == null) {
      return oauthError(HttpStatus.BAD_REQUEST, "invalid_request");
    }

    Optional<ClientCredentialsResolver.ClientCredentials> credentials = clientCredentialsResolver.resolve(request, body);
    String requesterClientId = credentials.map(ClientCredentialsResolver.ClientCredentials::clientId).orElse(null);
    String requesterClientSecret = credentials.map(ClientCredentialsResolver.ClientCredentials::clientSecret).orElse(null);
    if (requesterClientId == null) {
      return oauthError(HttpStatus.BAD_REQUEST, "invalid_request");
    }

    Optional<OidcClient> requesterClient = authorizationClientService.resolveClient(requesterClientId);
    if (requesterClient.isEmpty()
        || (requesterClient.get().isConfidential() && !requesterClient.get().verifySecret(requesterClientSecret))) {
      return oauthError(HttpStatus.UNAUTHORIZED, "invalid_client");
    }

    Optional<IntrospectionResponse> active = findActiveToken(token, tokenTypeHint, requesterClientId);
    if (active.isEmpty()) {
      return HttpResponse.ok(IntrospectionResponse.inactive());
    }
    return HttpResponse.ok(active.get());
  }

  private Optional<IntrospectionResponse> findActiveToken(String token, String tokenTypeHint, String requesterClientId) {
    if ("access_token".equals(tokenTypeHint)) {
      return findActiveAccessToken(token, requesterClientId);
    }
    if ("refresh_token".equals(tokenTypeHint)) {
      return findActiveRefreshToken(token, requesterClientId);
    }
    Optional<IntrospectionResponse> accessToken = findActiveAccessToken(token, requesterClientId);
    if (accessToken.isPresent()) {
      return accessToken;
    }
    return findActiveRefreshToken(token, requesterClientId);
  }

  private Optional<IntrospectionResponse> findActiveAccessToken(String token, String requesterClientId) {
    Optional<OAuth2AccessToken> oauth2AccessToken = oAuth2TokenService.findAccessToken(token)
        .filter(found -> requesterClientId.equals(found.getClientId()));
    if (oauth2AccessToken.isPresent()) {
      OAuth2AccessToken found = oauth2AccessToken.get();
      return Optional.of(IntrospectionResponse.active("access_token", found.getClientId(), found.getSubject(),
          found.getScopes(), found.getExpiresAt()));
    }
    Optional<OidcAccessToken> oidcAccessToken = oidcTokenService.findAccessToken(token)
        .filter(found -> requesterClientId.equals(found.getClientId()));
    if (oidcAccessToken.isPresent()) {
      OidcAccessToken found = oidcAccessToken.get();
      return Optional.of(IntrospectionResponse.active("access_token", found.getClientId(), found.getSubject(),
          found.getScopes(), found.getExpiresAt()));
    }
    return Optional.empty();
  }

  private Optional<IntrospectionResponse> findActiveRefreshToken(String token, String requesterClientId) {
    Optional<OAuth2RefreshToken> oauth2RefreshToken = oAuth2TokenService.findRefreshToken(token)
        .filter(found -> requesterClientId.equals(found.getClientId()));
    if (oauth2RefreshToken.isPresent()) {
      OAuth2RefreshToken found = oauth2RefreshToken.get();
      return Optional.of(IntrospectionResponse.active("refresh_token", found.getClientId(), found.getSubject(),
          found.getScopes(), found.getExpiresAt()));
    }
    Optional<OidcRefreshToken> oidcRefreshToken = oidcTokenService.findRefreshToken(token)
        .filter(found -> requesterClientId.equals(found.getClientId()));
    if (oidcRefreshToken.isPresent()) {
      OidcRefreshToken found = oidcRefreshToken.get();
      return Optional.of(IntrospectionResponse.active("refresh_token", found.getClientId(), found.getSubject(),
          found.getScopes(), found.getExpiresAt()));
    }
    return Optional.empty();
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
  private record IntrospectionResponse(boolean active, String scope, String client_id, String sub, Long exp,
                                       String token_type) {

    static IntrospectionResponse inactive() {
      return new IntrospectionResponse(false, null, null, null, null, null);
    }

    static IntrospectionResponse active(String tokenType, String clientId, String subject, Set<String> scopes,
        Instant expiresAt) {
      String scopeValue = (scopes == null || scopes.isEmpty()) ? null : String.join(" ", scopes);
      Long expValue = expiresAt == null ? null : expiresAt.getEpochSecond();
      return new IntrospectionResponse(true, scopeValue, clientId, subject, expValue, tokenType);
    }
  }

  @Serdeable
  private record OAuthErrorResponse(String error) {
  }
}
