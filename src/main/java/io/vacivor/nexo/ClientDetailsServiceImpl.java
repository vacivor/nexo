package io.vacivor.nexo;

import io.vacivor.nexo.dal.entity.ApplicationEntity;
import io.vacivor.nexo.security.auth.client.ClientDetails;
import io.vacivor.nexo.security.auth.client.ClientDetailsService;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class ClientDetailsServiceImpl implements ClientDetailsService {

  public ClientDetailsServiceImpl(ApplicationService applicationService) {
    this.applicationService = applicationService;
  }

  private final ApplicationService applicationService;

  @Override
  public ClientDetails loadClientByClientId(String clientId) {
    Optional<ApplicationEntity> byClientId = applicationService.findByClientId(clientId);
    return new ClientDetails() {
      @Override
      public String getClientId() {
        return byClientId.get().getClientId();
      }

      @Override
      public String getClientSecret() {
        return byClientId.get().getClientSecret();
      }
    };
  }

}
