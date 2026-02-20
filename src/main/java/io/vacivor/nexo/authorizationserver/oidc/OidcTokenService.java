package io.vacivor.nexo.authorizationserver.oidc;

import io.vacivor.nexo.core.RefreshTokenConsumeResult;
import io.vacivor.nexo.core.RefreshTokenConsumeStatus;
import io.vacivor.nexo.dal.entity.ApplicationEntity;
import io.vacivor.nexo.oidc.OidcAccessToken;
import io.vacivor.nexo.oidc.OidcClientService;
import io.vacivor.nexo.oidc.OidcConfiguration;
import io.vacivor.nexo.oidc.OidcRefreshToken;
import io.vacivor.nexo.oidc.store.OidcAccessTokenStore;
import io.vacivor.nexo.oidc.store.OidcRefreshTokenStore;
import jakarta.inject.Singleton;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Singleton
public class OidcTokenService {

  private final OidcConfiguration configuration;
  private final OidcClientService clientService;
  private final OidcAccessTokenStore accessTokenStore;
  private final OidcRefreshTokenStore refreshTokenStore;
  private final SecureRandom secureRandom = new SecureRandom();

  public OidcTokenService(OidcConfiguration configuration,
      OidcClientService clientService,
      OidcAccessTokenStore accessTokenStore,
      OidcRefreshTokenStore refreshTokenStore) {
    this.configuration = configuration;
    this.clientService = clientService;
    this.accessTokenStore = accessTokenStore;
    this.refreshTokenStore = refreshTokenStore;
  }

  public OidcAccessToken issueAccessToken(String subject, String clientId, Set<String> scopes) {
    return issueAccessToken(subject, clientId, scopes, null);
  }

  public OidcAccessToken issueAccessToken(String subject, String clientId, Set<String> scopes, String familyId) {
    String token = randomToken();
    Instant expiresAt = Instant.now().plus(configuration.getAccessTokenTtl());
    OidcAccessToken accessToken = new OidcAccessToken(token, subject, clientId, scopes, expiresAt, familyId);
    accessTokenStore.store(accessToken);
    return accessToken;
  }

  public Optional<OidcRefreshToken> issueRefreshToken(String subject, String clientId, Set<String> scopes) {
    return issueRefreshToken(subject, clientId, scopes, null);
  }

  public Optional<OidcRefreshToken> issueRefreshToken(String subject, String clientId, Set<String> scopes,
      String familyId) {
    if (!configuration.isRefreshTokenEnabled()) {
      return Optional.empty();
    }
    String token = randomToken();
    Instant expiresAt = Instant.now().plus(resolveRefreshTokenTtl(clientId));
    String resolvedFamilyId = (familyId == null || familyId.isBlank()) ? randomToken() : familyId;
    OidcRefreshToken refreshToken = new OidcRefreshToken(token, subject, clientId, scopes, expiresAt,
        resolvedFamilyId);
    refreshTokenStore.store(refreshToken);
    return Optional.of(refreshToken);
  }

  public Optional<OidcAccessToken> findAccessToken(String token) {
    return accessTokenStore.find(token).filter(t -> t.getExpiresAt().isAfter(Instant.now()));
  }

  public Optional<OidcRefreshToken> findRefreshToken(String token) {
    return refreshTokenStore.find(token).filter(t -> t.getExpiresAt().isAfter(Instant.now()));
  }

  public Optional<OidcRefreshToken> consumeRefreshToken(String token) {
    return refreshTokenStore.consume(token).filter(t -> t.getExpiresAt().isAfter(Instant.now()));
  }

  public RefreshTokenConsumeResult<OidcRefreshToken> consumeRefreshTokenSecure(String token) {
    RefreshTokenConsumeResult<OidcRefreshToken> result = refreshTokenStore.consumeWithStatus(token);
    if (result.getToken().isPresent() && result.getToken().get().getExpiresAt().isBefore(Instant.now())) {
      return RefreshTokenConsumeResult.status(RefreshTokenConsumeStatus.NOT_FOUND);
    }
    return result;
  }

  public void revokeAccessToken(String token) {
    if (token == null || token.isBlank()) {
      return;
    }
    accessTokenStore.revoke(token);
  }

  public void revokeRefreshToken(String token) {
    if (token == null || token.isBlank()) {
      return;
    }
    Optional<OidcRefreshToken> existing = refreshTokenStore.find(token);
    refreshTokenStore.revoke(token);
    existing.map(OidcRefreshToken::getFamilyId).ifPresent(this::revokeTokenFamily);
  }

  public void revokeTokenFamily(String familyId) {
    if (familyId == null || familyId.isBlank()) {
      return;
    }
    refreshTokenStore.revokeFamily(familyId);
    accessTokenStore.revokeByFamily(familyId);
  }

  public Set<String> resolveRefreshScopes(OidcRefreshToken refreshToken, String requestedScope) {
    if (requestedScope == null || requestedScope.isBlank()) {
      return refreshToken.getScopes();
    }
    Set<String> requested = new HashSet<>();
    for (String value : requestedScope.trim().split("\\s+")) {
      if (!value.isBlank()) {
        requested.add(value);
      }
    }
    if (!refreshToken.getScopes().containsAll(requested)) {
      return Set.of();
    }
    return requested;
  }

  private String randomToken() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private Duration resolveRefreshTokenTtl(String clientId) {
    return resolveClientEntity(clientId)
        .map(ApplicationEntity::getRefreshTokenExpiration)
        .filter(value -> value != null && value > 0)
        .map(Duration::ofSeconds)
        .orElse(configuration.getRefreshTokenTtl());
  }

  private Optional<ApplicationEntity> resolveClientEntity(String clientId) {
    if (clientId == null || clientId.isBlank()) {
      return Optional.empty();
    }
    return clientService.findEntityByClientId(clientId);
  }
}
