package io.vacivor.nexo.oidc;

import io.vacivor.nexo.core.RegisteredClient;
import io.vacivor.nexo.core.RegisteredClientRepository;
import io.vacivor.nexo.dal.entity.ApplicationEntity;
import io.vacivor.nexo.dal.repository.ApplicationRepository;
import jakarta.inject.Singleton;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class OidcClientService implements RegisteredClientRepository {

  private final ApplicationRepository applicationRepository;

  public OidcClientService(ApplicationRepository applicationRepository) {
    this.applicationRepository = applicationRepository;
  }

  public Optional<ApplicationEntity> findEntityByClientId(String clientId) {
    return applicationRepository.findByClientId(clientId);
  }

  @Override
  public Optional<RegisteredClient> findByClientId(String clientId) {
    return findEntityByClientId(clientId).map(entity -> new OidcClient(entity, this));
  }

  public boolean validateSecret(ApplicationEntity client, String secret) {
    if (client.getClientSecret() == null) {
      return false;
    }
    return client.getClientSecret().equals(secret);
  }

  public boolean isRedirectUriAllowed(ApplicationEntity client, String redirectUri) {
    if (client.getRedirectUris() == null || redirectUri == null) {
      return false;
    }
    Optional<String> candidate = normalizeRedirectUri(redirectUri);
    if (candidate.isEmpty()) {
      return false;
    }
    Set<String> allowed = Arrays.stream(client.getRedirectUris().split(","))
        .map(this::normalizeRedirectUri)
        .flatMap(Optional::stream)
        .collect(Collectors.toSet());
    return allowed.contains(candidate.get());
  }

  private Optional<String> normalizeRedirectUri(String value) {
    if (value == null) {
      return Optional.empty();
    }
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      return Optional.empty();
    }
    try {
      URI uri = URI.create(trimmed);
      if (!uri.isAbsolute()) {
        return Optional.empty();
      }
      if (uri.getFragment() != null) {
        return Optional.empty();
      }
      return Optional.of(trimmed);
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }
}
