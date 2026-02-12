package io.vacivor.nexo.security.oidc;

import io.vacivor.nexo.ApplicationEntity;
import io.vacivor.nexo.ApplicationRepository;
import jakarta.inject.Singleton;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class OidcClientService {

  private final ApplicationRepository applicationRepository;

  public OidcClientService(ApplicationRepository applicationRepository) {
    this.applicationRepository = applicationRepository;
  }

  public Optional<ApplicationEntity> findByClientId(String clientId) {
    return applicationRepository.findByClientId(clientId);
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
