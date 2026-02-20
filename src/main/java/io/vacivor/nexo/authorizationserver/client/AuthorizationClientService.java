package io.vacivor.nexo.authorizationserver.client;

import io.vacivor.nexo.oidc.OidcClient;
import io.vacivor.nexo.oidc.OidcClientService;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class AuthorizationClientService {

  private final OidcClientService oidcClientService;

  public AuthorizationClientService(OidcClientService oidcClientService) {
    this.oidcClientService = oidcClientService;
  }

  public Optional<OidcClient> resolveClient(String clientId) {
    return oidcClientService.findEntityByClientId(clientId)
        .map(client -> new OidcClient(client, oidcClientService));
  }
}
