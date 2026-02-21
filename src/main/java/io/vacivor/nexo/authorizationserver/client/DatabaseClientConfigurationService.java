package io.vacivor.nexo.authorizationserver.client;

import io.vacivor.nexo.dal.entity.ApplicationEntity;
import io.vacivor.nexo.dal.repository.ApplicationRepository;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class DatabaseClientConfigurationService implements ClientConfigurationService {

  private final ApplicationRepository applicationRepository;

  public DatabaseClientConfigurationService(ApplicationRepository applicationRepository) {
    this.applicationRepository = applicationRepository;
  }

  @Override
  public Optional<Integer> findRefreshTokenExpirationSeconds(String clientId) {
    Optional<ApplicationEntity> client = findClient(clientId);
    if (client.isEmpty()) {
      return Optional.empty();
    }
    Integer value = client.get().getRefreshTokenExpiration();
    if (value == null || value <= 0) {
      return Optional.empty();
    }
    return Optional.of(value);
  }

  @Override
  public Optional<Integer> findIdTokenExpirationSeconds(String clientId) {
    Optional<ApplicationEntity> client = findClient(clientId);
    if (client.isEmpty()) {
      return Optional.empty();
    }
    Integer value = client.get().getIdTokenExpiration();
    if (value == null || value <= 0) {
      return Optional.empty();
    }
    return Optional.of(value);
  }

  private Optional<ApplicationEntity> findClient(String clientId) {
    if (clientId == null || clientId.isBlank()) {
      return Optional.empty();
    }
    return applicationRepository.findByClientId(clientId);
  }
}
