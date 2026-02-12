package io.vacivor.nexo.security.oidc;

import io.vacivor.nexo.dal.entity.ApplicationEntity;

public class OidcClient {

  private final ApplicationEntity entity;
  private final OidcClientService clientService;

  public OidcClient(ApplicationEntity entity, OidcClientService clientService) {
    this.entity = entity;
    this.clientService = clientService;
  }

  public String getClientId() {
    return entity.getClientId();
  }

  public boolean verifySecret(String secret) {
    return clientService.validateSecret(entity, secret);
  }

  public boolean isRedirectUriAllowed(String redirectUri) {
    return clientService.isRedirectUriAllowed(entity, redirectUri);
  }
}
