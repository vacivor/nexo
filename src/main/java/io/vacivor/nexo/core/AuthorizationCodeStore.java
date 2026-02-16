package io.vacivor.nexo.core;

import java.util.Optional;

public interface AuthorizationCodeStore<T extends AuthorizationCode> {

  void store(T code);

  Optional<T> consume(String code);
}
