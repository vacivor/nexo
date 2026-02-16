package io.vacivor.nexo.core;

import java.util.Optional;

public interface RefreshTokenStore<T extends RefreshToken> {

  void store(T token);

  Optional<T> consume(String token);
}
