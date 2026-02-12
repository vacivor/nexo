package io.vacivor.nexo.security.providers;

import io.vacivor.nexo.dal.entity.IdentityProviderEntity;
import io.vacivor.nexo.dal.repository.IdentityProviderRepository;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class IdentityProviderService {

  private final IdentityProviderRepository repository;

  public IdentityProviderService(IdentityProviderRepository repository) {
    this.repository = repository;
  }

  public IdentityProviderEntity create(IdentityProviderEntity input) {
    IdentityProviderEntity entity = new IdentityProviderEntity();
    entity.setUuid(UUID.randomUUID().toString());
    copy(input, entity);
    return repository.save(entity);
  }

  public Optional<IdentityProviderEntity> update(String uuid, IdentityProviderEntity input) {
    return repository.findByUuid(uuid).map(existing -> {
      copy(input, existing);
      return repository.update(existing);
    });
  }

  public Optional<IdentityProviderEntity> findByUuid(String uuid) {
    return repository.findByUuid(uuid);
  }

  public List<IdentityProviderEntity> findAll() {
    return repository.findAll();
  }

  public boolean deleteByUuid(String uuid) {
    Optional<IdentityProviderEntity> existing = repository.findByUuid(uuid);
    if (existing.isEmpty()) {
      return false;
    }
    repository.delete(existing.get());
    return true;
  }

  public Optional<IdentityProviderEntity> resolveEnabled(IdentityProviderProtocol protocol, String provider) {
    String normalizedProvider = normalizeProvider(provider);
    if (normalizedProvider == null) {
      return Optional.empty();
    }
    return repository.findByProtocolAndProviderAndEnabledTrue(protocol, normalizedProvider);
  }

  public Optional<IdentityProviderEntity> setEnabled(String uuid, boolean enabled) {
    return repository.findByUuid(uuid).map(existing -> {
      existing.setEnabled(enabled);
      return repository.update(existing);
    });
  }

  private void copy(IdentityProviderEntity source, IdentityProviderEntity target) {
    target.setProtocol(source.getProtocol());
    target.setProvider(normalizeProvider(source.getProvider()));
    target.setDisplayName(normalize(source.getDisplayName()));
    target.setEnabled(source.getEnabled() == null ? Boolean.TRUE : source.getEnabled());
    target.setClientId(normalize(source.getClientId()));
    target.setClientSecret(normalize(source.getClientSecret()));
    target.setAuthorizationUri(normalize(source.getAuthorizationUri()));
    target.setTokenUri(normalize(source.getTokenUri()));
    target.setUserInfoUri(normalize(source.getUserInfoUri()));
    target.setJwksUri(normalize(source.getJwksUri()));
    target.setIssuer(normalize(source.getIssuer()));
    target.setRedirectUri(normalize(source.getRedirectUri()));
    target.setScopes(normalize(source.getScopes()));
    target.setExtraConfig(normalize(source.getExtraConfig()));
  }

  private String normalizeProvider(String value) {
    String normalized = normalize(value);
    return normalized == null ? null : normalized.toLowerCase();
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
