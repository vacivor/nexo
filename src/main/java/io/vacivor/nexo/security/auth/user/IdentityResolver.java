package io.vacivor.nexo.security.auth.user;

import java.util.Optional;

public interface IdentityResolver {

  Optional<UserDetails> resolve(String identifier);
}
