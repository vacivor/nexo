package io.vacivor.nexo.authorizationserver.client;

import io.vacivor.nexo.core.ClientDetails;
import io.vacivor.nexo.core.ClientDetailsService;
import io.vacivor.nexo.dal.entity.ApplicationEntity;
import io.vacivor.nexo.dal.repository.ApplicationRepository;
import io.vacivor.nexo.oidc.OidcClient;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class DatabaseClientDetailsService implements ClientDetailsService {

  private final ApplicationRepository applicationRepository;

  public DatabaseClientDetailsService(ApplicationRepository applicationRepository) {
    this.applicationRepository = applicationRepository;
  }

  @Override
  public Optional<ClientDetails> findByClientId(String clientId) {
    Optional<ApplicationEntity> application = applicationRepository.findByClientId(clientId);
    if (application.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(new OidcClient(application.get()));
  }
}
