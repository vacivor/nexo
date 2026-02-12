package io.vacivor.nexo.security.oidc;

import io.micronaut.http.HttpRequest;
import io.vacivor.nexo.security.oidc.store.OidcAccessTokenStore;
import io.vacivor.nexo.security.oidc.store.OidcAuthorizationCodeStore;
import io.vacivor.nexo.security.user.UserEntity;
import io.vacivor.nexo.security.user.UserRepository;
import jakarta.inject.Singleton;
import java.security.SecureRandom;
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
  private final OidcJwtSigner jwtSigner;
  private final UserRepository userRepository;
  private final SecureRandom secureRandom = new SecureRandom();

  public OidcService(OidcConfiguration configuration,
      OidcClientService clientService,
      OidcAuthorizationCodeStore codeStore,
      OidcAccessTokenStore accessTokenStore,
      OidcJwtSigner jwtSigner,
      UserRepository userRepository) {
    this.configuration = configuration;
    this.clientService = clientService;
    this.codeStore = codeStore;
    this.accessTokenStore = accessTokenStore;
    this.jwtSigner = jwtSigner;
    this.userRepository = userRepository;
  }

  public Map<String, Object> discovery(HttpRequest<?> request) {
    String issuer = configuration.getIssuer();
    String baseUrl = issuer;
    return Map.of(
        "issuer", issuer,
        "authorization_endpoint", baseUrl + "/oauth/authorize",
        "token_endpoint", baseUrl + "/oauth/token",
        "userinfo_endpoint", baseUrl + "/oauth/userinfo",
        "response_types_supported", new String[] {"code"},
        "subject_types_supported", new String[] {"public"},
        "id_token_signing_alg_values_supported", new String[] {"HS256"}
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

  public Optional<OidcAccessToken> findAccessToken(String token) {
    return accessTokenStore.find(token).filter(t -> t.getExpiresAt().isAfter(Instant.now()));
  }

  public String issueIdToken(String subject, String audience, String nonce) {
    Instant now = Instant.now();
    Instant exp = now.plus(configuration.getIdTokenTtl());
    Map<String, Object> claims = jwtSigner.buildIdTokenClaims(configuration.getIssuer(), subject,
        audience, now, exp, nonce);
    return jwtSigner.signHs256(configuration.getHmacSecret(), claims);
  }

  public Optional<UserEntity> findUserByUsername(String username) {
    return userRepository.findByUsername(username);
  }

  public Optional<OidcClient> resolveClient(String clientId) {
    return clientService.findByClientId(clientId).map(client -> new OidcClient(client,
        clientService));
  }

  private String randomToken() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
