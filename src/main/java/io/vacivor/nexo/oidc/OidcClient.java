package io.vacivor.nexo.oidc;

import io.vacivor.nexo.core.ClientDetails;
import io.vacivor.nexo.dal.entity.ApplicationEntity;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class OidcClient implements ClientDetails {

  private final ApplicationEntity entity;
  private final OidcClientService clientService;

  public OidcClient(ApplicationEntity entity, OidcClientService clientService) {
    this.entity = entity;
    this.clientService = clientService;
  }

  public String getClientId() {
    return entity.getClientId();
  }

  @Override
  public String getClientSecret() {
    return entity.getClientSecret();
  }

  @Override
  public Set<String> getRedirectUris() {
    String redirectUris = entity.getRedirectUris();
    if (redirectUris == null || redirectUris.isBlank()) {
      return Set.of();
    }
    return Arrays.stream(redirectUris.split(","))
        .map(String::trim)
        .filter(value -> !value.isBlank())
        .collect(Collectors.toSet());
  }

  @Override
  public Set<String> getScopes() {
    return Set.of();
  }

  @Override
  public boolean isConfidential() {
    return entity.getClientSecret() != null && !entity.getClientSecret().isBlank();
  }

  public boolean verifySecret(String secret) {
    return clientService.validateSecret(entity, secret);
  }

  public boolean isRedirectUriAllowed(String redirectUri) {
    return clientService.isRedirectUriAllowed(entity, redirectUri);
  }
}
