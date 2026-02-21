package io.vacivor.nexo.oidc;

import io.vacivor.nexo.core.ClientDetails;
import io.vacivor.nexo.dal.entity.ApplicationEntity;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class OidcClient implements ClientDetails {

  private final ApplicationEntity entity;

  public OidcClient(ApplicationEntity entity) {
    this.entity = entity;
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
    String actual = entity.getClientSecret();
    return actual != null && actual.equals(secret);
  }

  public boolean isRedirectUriAllowed(String redirectUri) {
    if (entity.getRedirectUris() == null || redirectUri == null) {
      return false;
    }
    Optional<String> candidate = normalizeRedirectUri(redirectUri);
    if (candidate.isEmpty()) {
      return false;
    }
    Set<String> allowed = Arrays.stream(entity.getRedirectUris().split(","))
        .map(OidcClient::normalizeRedirectUri)
        .flatMap(Optional::stream)
        .collect(Collectors.toSet());
    return allowed.contains(candidate.get());
  }

  private static Optional<String> normalizeRedirectUri(String value) {
    if (value == null) {
      return Optional.empty();
    }
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      return Optional.empty();
    }
    try {
      URI uri = URI.create(trimmed);
      if (!uri.isAbsolute() || uri.getFragment() != null) {
        return Optional.empty();
      }
      return Optional.of(trimmed);
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }
}
