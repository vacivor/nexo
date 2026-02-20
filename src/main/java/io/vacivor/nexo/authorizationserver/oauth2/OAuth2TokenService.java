package io.vacivor.nexo.authorizationserver.oauth2;

import io.vacivor.nexo.core.RefreshTokenConsumeResult;
import io.vacivor.nexo.core.RefreshTokenConsumeStatus;
import io.vacivor.nexo.dal.entity.ApplicationEntity;
import io.vacivor.nexo.dal.repository.ApplicationRepository;
import io.vacivor.nexo.oauth2.store.OAuth2AccessTokenStore;
import io.vacivor.nexo.oauth2.store.OAuth2RefreshTokenStore;
import io.vacivor.nexo.oauth2.token.OAuth2AccessToken;
import io.vacivor.nexo.oauth2.token.OAuth2RefreshToken;
import io.vacivor.nexo.oidc.OidcConfiguration;
import jakarta.inject.Singleton;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Singleton
public class OAuth2TokenService {

  private final OidcConfiguration configuration;
  private final ApplicationRepository applicationRepository;
  private final OAuth2AccessTokenStore accessTokenStore;
  private final OAuth2RefreshTokenStore refreshTokenStore;
  private final SecureRandom secureRandom = new SecureRandom();

  public OAuth2TokenService(OidcConfiguration configuration,
      ApplicationRepository applicationRepository,
      OAuth2AccessTokenStore accessTokenStore,
      OAuth2RefreshTokenStore refreshTokenStore) {
    this.configuration = configuration;
    this.applicationRepository = applicationRepository;
    this.accessTokenStore = accessTokenStore;
    this.refreshTokenStore = refreshTokenStore;
  }

  public OAuth2AccessToken issueAccessToken(String subject, String clientId, Set<String> scopes) {
    return issueAccessToken(subject, clientId, scopes, null);
  }

  public OAuth2AccessToken issueAccessToken(String subject, String clientId, Set<String> scopes,
      String familyId) {
    String token = randomToken();
    Instant expiresAt = Instant.now().plus(configuration.getAccessTokenTtl());
    OAuth2AccessToken accessToken = new OAuth2AccessToken(token, subject, clientId, scopes, expiresAt,
        familyId);
    accessTokenStore.store(accessToken);
    return accessToken;
  }

  public Optional<OAuth2RefreshToken> issueRefreshToken(String subject, String clientId, Set<String> scopes) {
    return issueRefreshToken(subject, clientId, scopes, null);
  }

  public Optional<OAuth2RefreshToken> issueRefreshToken(String subject, String clientId, Set<String> scopes,
      String familyId) {
    if (!configuration.isRefreshTokenEnabled()) {
      return Optional.empty();
    }
    String token = randomToken();
    Instant expiresAt = Instant.now().plus(resolveRefreshTokenTtl(clientId));
    String resolvedFamilyId = (familyId == null || familyId.isBlank()) ? randomToken() : familyId;
    OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(token, subject, clientId, scopes, expiresAt,
        resolvedFamilyId);
    refreshTokenStore.store(refreshToken);
    return Optional.of(refreshToken);
  }

  public Optional<OAuth2AccessToken> findAccessToken(String token) {
    return accessTokenStore.find(token).filter(t -> t.getExpiresAt().isAfter(Instant.now()));
  }

  public Optional<OAuth2RefreshToken> findRefreshToken(String token) {
    return refreshTokenStore.find(token).filter(t -> t.getExpiresAt().isAfter(Instant.now()));
  }

  public Optional<OAuth2RefreshToken> consumeRefreshToken(String token) {
    return refreshTokenStore.consume(token).filter(t -> t.getExpiresAt().isAfter(Instant.now()));
  }

  public RefreshTokenConsumeResult<OAuth2RefreshToken> consumeRefreshTokenSecure(String token) {
    RefreshTokenConsumeResult<OAuth2RefreshToken> result = refreshTokenStore.consumeWithStatus(token);
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
    Optional<OAuth2RefreshToken> existing = refreshTokenStore.find(token);
    refreshTokenStore.revoke(token);
    existing.map(OAuth2RefreshToken::getFamilyId).ifPresent(this::revokeTokenFamily);
  }

  public void revokeTokenFamily(String familyId) {
    if (familyId == null || familyId.isBlank()) {
      return;
    }
    refreshTokenStore.revokeFamily(familyId);
    accessTokenStore.revokeByFamily(familyId);
  }

  public Set<String> resolveRefreshScopes(OAuth2RefreshToken refreshToken, String requestedScope) {
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
    return applicationRepository.findByClientId(clientId);
  }
}
