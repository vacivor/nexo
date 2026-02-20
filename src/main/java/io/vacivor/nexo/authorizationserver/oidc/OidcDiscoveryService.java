package io.vacivor.nexo.authorizationserver.oidc;

import io.vacivor.nexo.oidc.OidcConfiguration;
import jakarta.inject.Singleton;
import java.util.Map;

@Singleton
public class OidcDiscoveryService {

  private final OidcConfiguration configuration;

  public OidcDiscoveryService(OidcConfiguration configuration) {
    this.configuration = configuration;
  }

  public Map<String, Object> discovery() {
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
        "id_token_signing_alg_values_supported", new String[] {signingAlg});
  }
}
