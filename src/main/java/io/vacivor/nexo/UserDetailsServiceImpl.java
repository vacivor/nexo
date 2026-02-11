package io.vacivor.nexo;

import io.vacivor.nexo.security.auth.user.UserDetails;
import io.vacivor.nexo.security.auth.user.UserDetailsService;
import io.vacivor.nexo.security.user.UserRepository;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class UserDetailsServiceImpl implements UserDetailsService {

  private UserRepository userRepository;

  @Override
  public Optional<UserDetails> loadUserByUsername(String username) {
    return Optional.empty();
  }

  @Override
  public Optional<UserDetails> loadUserByPhone(String username) {
    return Optional.empty();
  }

  @Override
  public Optional<UserDetails> loadUserByEmail(String username) {
    return Optional.empty();
  }
}
