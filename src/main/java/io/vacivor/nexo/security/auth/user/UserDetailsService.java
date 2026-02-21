package io.vacivor.nexo.security.auth.user;

import java.util.Optional;

public interface UserDetailsService {

  Optional<UserDetails> loadUserByUsername(String username);

  Optional<UserDetails> loadUserByPhone(String username);

  Optional<UserDetails> loadUserByEmail(String username);

}
