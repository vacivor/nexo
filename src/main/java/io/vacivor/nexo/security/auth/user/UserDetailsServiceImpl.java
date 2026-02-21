package io.vacivor.nexo.security.auth.user;

import io.vacivor.nexo.dal.entity.UserEntity;
import io.vacivor.nexo.dal.repository.UserRepository;
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
    Optional<UserEntity> user = userRepository.findByUsername(username);
    if (user.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(toUserDetails(user.get()));
  }

  @Override
  public Optional<UserDetails> loadUserByPhone(String username) {
    Optional<UserEntity> user = userRepository.findByPhone(username);
    if (user.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(toUserDetails(user.get()));
  }

  @Override
  public Optional<UserDetails> loadUserByEmail(String username) {
    Optional<UserEntity> user = userRepository.findByEmail(username);
    if (user.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(toUserDetails(user.get()));
  }

  private UserDetails toUserDetails(UserEntity user) {
    UserDetails details = new UserDetails();
    details.setUsername(user.getUsername());
    details.setPassword(user.getPassword());
    return details;
  }
}
