package io.vacivor.nexo.security.auth.core;

import java.util.Optional;

public interface AuthenticationProvider<T extends AuthenticationToken> {

  boolean supports(AuthenticationToken token);

  Optional<Authentication> authenticate(T token);
}
