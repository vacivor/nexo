package io.vacivor.nexo.authorizationserver.client;

import java.util.Optional;

public interface ClientConfigurationService {

  Optional<Integer> findRefreshTokenExpirationSeconds(String clientId);

  Optional<Integer> findIdTokenExpirationSeconds(String clientId);
}
