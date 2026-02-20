package io.vacivor.nexo.authorizationserver.user;

import io.vacivor.nexo.dal.entity.UserEntity;
import io.vacivor.nexo.dal.repository.UserRepository;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class UserInfoService {

  private final UserRepository userRepository;

  public UserInfoService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Optional<UserEntity> findUserByUsername(String username) {
    return userRepository.findByUsername(username);
  }
}
