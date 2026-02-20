package io.vacivor.nexo.authorizationserver.oidc;

import io.vacivor.nexo.dal.entity.ApplicationEntity;
import io.vacivor.nexo.oidc.OidcClientService;
import io.vacivor.nexo.oidc.OidcConfiguration;
import io.vacivor.nexo.oidc.OidcJwtSigner;
import io.vacivor.nexo.oidc.OidcKeyService;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Singleton
public class OidcIdTokenService {

  private final OidcConfiguration configuration;
  private final OidcClientService clientService;
  private final OidcJwtSigner jwtSigner;
  private final OidcKeyService keyService;

  public OidcIdTokenService(OidcConfiguration configuration,
      OidcClientService clientService,
      OidcJwtSigner jwtSigner,
      OidcKeyService keyService) {
    this.configuration = configuration;
    this.clientService = clientService;
    this.jwtSigner = jwtSigner;
    this.keyService = keyService;
  }

  public String issueIdToken(String subject, String audience, String nonce) {
    Instant now = Instant.now();
    Instant exp = now.plus(resolveIdTokenTtl(audience));
    Map<String, Object> claims = jwtSigner.buildIdTokenClaims(configuration.getIssuer(), subject, audience, now, exp,
        nonce);
    if ("RS256".equalsIgnoreCase(configuration.getSigningAlgorithm())) {
      return jwtSigner.signRs256(keyService.getPrivateKey(), keyService.getKeyId(), claims);
    }
    return jwtSigner.signHs256(configuration.getHmacSecret(), claims);
  }

  private Duration resolveIdTokenTtl(String clientId) {
    return resolveClientEntity(clientId)
        .map(ApplicationEntity::getIdTokenExpiration)
        .filter(value -> value != null && value > 0)
        .map(Duration::ofSeconds)
        .orElse(configuration.getIdTokenTtl());
  }

  private Optional<ApplicationEntity> resolveClientEntity(String clientId) {
    if (clientId == null || clientId.isBlank()) {
      return Optional.empty();
    }
    return clientService.findEntityByClientId(clientId);
  }
}
