package io.vacivor.nexo.core;

import java.util.Optional;

public interface ClientDetailsService {

  Optional<ClientDetails> findByClientId(String clientId);
}
