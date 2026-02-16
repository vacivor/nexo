package io.vacivor.nexo.core;

import java.util.Optional;

public interface RegisteredClientRepository {

  Optional<RegisteredClient> findByClientId(String clientId);
}
