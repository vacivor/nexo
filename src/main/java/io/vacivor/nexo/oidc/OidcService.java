package io.vacivor.nexo.oidc;

import io.micronaut.http.HttpRequest;
import io.vacivor.nexo.dal.entity.ApplicationEntity;
import io.vacivor.nexo.oidc.store.OidcAccessTokenStore;
import io.vacivor.nexo.oidc.store.OidcAuthorizationCodeStore;
import io.vacivor.nexo.oidc.store.OidcRefreshTokenStore;
import io.vacivor.nexo.dal.entity.UserEntity;
import io.vacivor.nexo.dal.repository.TenantUserRepository;
import io.vacivor.nexo.dal.repository.UserRepository;
import jakarta.inject.Singleton;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Singleton
public class OidcService {

  private final OidcConfiguration configuration;
  private final OidcClientService clientService;
  private final OidcAuthorizationCodeStore codeStore;
  private final OidcAccessTokenStore accessTokenStore;
  private final OidcRefreshTokenStore refreshTokenStore;
  private final OidcJwtSigner jwtSigner;
  private final OidcKeyService keyService;
  private final UserRepository userRepository;
  private final TenantUserRepository tenantUserRepository;
  private final SecureRandom secureRandom = new SecureRandom();

  public OidcService(OidcConfiguration configuration,
      OidcClientService clientService,
      OidcAuthorizationCodeStore codeStore,
      OidcAccessTokenStore accessTokenStore,
      OidcRefreshTokenStore refreshTokenStore,
      OidcJwtSigner jwtSigner,
      OidcKeyService keyService,
      UserRepository userRepository,
      TenantUserRepository tenantUserRepository) {
    this.configuration = configuration;
    this.clientService = clientService;
    this.codeStore = codeStore;
    this.accessTokenStore = accessTokenStore;
    this.refreshTokenStore = refreshTokenStore;
    this.jwtSigner = jwtSigner;
    this.keyService = keyService;
    this.userRepository = userRepository;
    this.tenantUserRepository = tenantUserRepository;
  }

  public Map<String, Object> discovery(HttpRequest<?> request) {
    String issuer = configuration.getIssuer();
    String baseUrl = issuer;
    String signingAlg = configuration.getSigningAlgorithm();
    return Map.of(
        "issuer", issuer,
        "authorization_endpoint", baseUrl + "/oidc/authorize",
        "token_endpoint", baseUrl + "/oidc/token",
        "userinfo_endpoint", baseUrl + "/oidc/userinfo",
        "jwks_uri", baseUrl + "/oidc/jwks",
        "response_types_supported", new String[] {"code"},
        "subject_types_supported", new String[] {"public"},
        "id_token_signing_alg_values_supported", new String[] {signingAlg}
    );
  }

  public OidcAuthorizationCode issueAuthorizationCode(String clientId, String redirectUri,
      String subject, Set<String> scopes, String nonce) {
    String code = randomToken();
    Instant expiresAt = Instant.now().plus(configuration.getCodeTtl());
    OidcAuthorizationCode authCode = new OidcAuthorizationCode(code, clientId, redirectUri,
        subject, scopes, nonce, expiresAt);
    codeStore.store(authCode);
    return authCode;
  }

  public Optional<OidcAuthorizationCode> consumeCode(String code) {
    return codeStore.consume(code).filter(c -> c.getExpiresAt().isAfter(Instant.now()));
  }

  public OidcAccessToken issueAccessToken(String subject, String clientId, Set<String> scopes) {
    String token = randomToken();
    Instant expiresAt = Instant.now().plus(configuration.getAccessTokenTtl());
    OidcAccessToken accessToken = new OidcAccessToken(token, subject, clientId, scopes, expiresAt);
    accessTokenStore.store(accessToken);
    return accessToken;
  }

  public Optional<OidcRefreshToken> issueRefreshToken(String subject, String clientId, Set<String> scopes) {
    if (!configuration.isRefreshTokenEnabled()) {
      return Optional.empty();
    }
    String token = randomToken();
    Instant expiresAt = Instant.now().plus(resolveRefreshTokenTtl(clientId));
    OidcRefreshToken refreshToken = new OidcRefreshToken(token, subject, clientId, scopes, expiresAt);
    refreshTokenStore.store(refreshToken);
    return Optional.of(refreshToken);
  }

  public Optional<OidcAccessToken> findAccessToken(String token) {
    return accessTokenStore.find(token).filter(t -> t.getExpiresAt().isAfter(Instant.now()));
  }

  public Optional<OidcRefreshToken> consumeRefreshToken(String token) {
    return refreshTokenStore.consume(token).filter(t -> t.getExpiresAt().isAfter(Instant.now()));
  }

  public String issueIdToken(String subject, String audience, String nonce) {
    Instant now = Instant.now();
    Instant exp = now.plus(resolveIdTokenTtl(audience));
    Map<String, Object> claims = jwtSigner.buildIdTokenClaims(configuration.getIssuer(), subject,
        audience, now, exp, nonce);
    if ("RS256".equalsIgnoreCase(configuration.getSigningAlgorithm())) {
      return jwtSigner.signRs256(keyService.getPrivateKey(), keyService.getKeyId(), claims);
    }
    return jwtSigner.signHs256(configuration.getHmacSecret(), claims);
  }

  public Optional<UserEntity> findUserByUsername(String username) {
    return userRepository.findByUsername(username);
  }

  public Optional<OidcClient> resolveClient(String clientId) {
    return clientService.findEntityByClientId(clientId).map(client -> new OidcClient(client,
        clientService));
  }

  public boolean isUserTenantAllowedForClient(String username, String clientId) {
    if (username == null || username.isBlank() || clientId == null || clientId.isBlank()) {
      return false;
    }
    Optional<UserEntity> user = userRepository.findByUsername(username);
    Optional<ApplicationEntity> client = clientService.findEntityByClientId(clientId);
    if (user.isEmpty() || client.isEmpty()) {
      return false;
    }
    if (Boolean.TRUE.equals(user.get().getIsDeleted())) {
      return false;
    }
    String clientTenantId = normalize(client.get().getTenantId());
    if (clientTenantId == null) {
      return true;
    }
    return tenantUserRepository.existsActiveMembership(
        user.get().getId(),
        clientTenantId);
  }

  private String randomToken() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private Duration resolveIdTokenTtl(String clientId) {
    return resolveClientEntity(clientId)
        .map(ApplicationEntity::getIdTokenExpiration)
        .filter(value -> value != null && value > 0)
        .map(Duration::ofSeconds)
        .orElse(configuration.getIdTokenTtl());
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

  private String normalize(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
