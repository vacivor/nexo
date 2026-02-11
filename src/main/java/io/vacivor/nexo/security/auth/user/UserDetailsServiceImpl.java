package io.vacivor.nexo.security.auth.user;

import io.vacivor.nexo.security.user.UserEntity;
import io.vacivor.nexo.security.user.UserRepository;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  public UserDetailsServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Optional<UserDetails> loadUserByUsername(String username) {
    return userRepository.findByUsername(username).map(this::toUserDetails);
  }

  @Override
  public Optional<UserDetails> loadUserByPhone(String username) {
    return userRepository.findByPhone(username).map(this::toUserDetails);
  }

  @Override
  public Optional<UserDetails> loadUserByEmail(String username) {
    return userRepository.findByEmail(username).map(this::toUserDetails);
  }

  private UserDetails toUserDetails(UserEntity user) {
    UserDetails details = new UserDetails();
    details.setUsername(user.getUsername());
    details.setPassword(user.getPassword());
    return details;
  }
}
