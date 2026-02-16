package io.vacivor.nexo.core;

import java.util.Optional;

public interface AccessTokenStore<T extends AccessToken> {

  void store(T token);

  Optional<T> find(String token);
}
