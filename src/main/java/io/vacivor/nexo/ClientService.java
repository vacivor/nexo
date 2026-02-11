package io.vacivor.nexo;

import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class ClientService {

  private ApplicationService applicationService;

  private ClientDetails loadClientByClientId(String clientId) {
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
