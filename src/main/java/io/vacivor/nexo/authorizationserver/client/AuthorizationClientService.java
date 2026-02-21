package io.vacivor.nexo.authorizationserver.client;

import io.vacivor.nexo.core.ClientDetails;
import io.vacivor.nexo.core.ClientDetailsService;
import io.vacivor.nexo.oidc.OidcClient;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class AuthorizationClientService {

  private final ClientDetailsService clientDetailsService;

  public AuthorizationClientService(ClientDetailsService clientDetailsService) {
    this.clientDetailsService = clientDetailsService;
  }

  public Optional<OidcClient> resolveClient(String clientId) {
    return clientDetailsService.findByClientId(clientId)
        .filter(OidcClient.class::isInstance)
        .map(OidcClient.class::cast);
  }

  public Optional<ClientDetails> resolveClientDetails(String clientId) {
    return clientDetailsService.findByClientId(clientId);
  }
}
