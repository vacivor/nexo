package io.vacivor.nexo.admin.user;

import io.vacivor.nexo.security.auth.PasswordEncoder;
import io.vacivor.nexo.security.user.UserEntity;
import io.vacivor.nexo.security.user.UserRepository;
import jakarta.inject.Singleton;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Singleton
public class AdminUserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public List<UserEntity> listUsers() {
    return userRepository.findByDeletedNotTrue();
  }

  public Optional<UserEntity> findUser(Long id) {
    return userRepository.findByIdAndDeletedNotTrue(id);
  }

  public Optional<UserEntity> createUser(String username, String email, String phone, String password) {
    String normalizedUsername = normalize(username);
    String normalizedEmail = normalize(email);
    String normalizedPhone = normalize(phone);
    String normalizedPassword = normalize(password);

    if (normalizedUsername == null || normalizedPassword == null) {
      return Optional.empty();
    }
    if (!isUniqueForCreate(normalizedUsername, normalizedEmail, normalizedPhone)) {
      return Optional.empty();
    }

    UserEntity user = new UserEntity();
    user.setUsername(normalizedUsername);
    user.setEmail(normalizedEmail);
    user.setPhone(normalizedPhone);
    user.setPassword(passwordEncoder.encode(normalizedPassword));
    user.setDeleted(Boolean.FALSE);
    return Optional.of(userRepository.save(user));
  }

  public Optional<UserEntity> updateUser(Long id, String username, String email, String phone) {
    Optional<UserEntity> existing = userRepository.findByIdAndDeletedNotTrue(id);
    if (existing.isEmpty()) {
      return Optional.empty();
    }

    UserEntity user = existing.get();
    String normalizedUsername = normalize(username);
    String normalizedEmail = normalize(email);
    String normalizedPhone = normalize(phone);

    if (normalizedUsername == null) {
      return Optional.empty();
    }
    if (!isUniqueForUpdate(id, normalizedUsername, normalizedEmail, normalizedPhone)) {
      return Optional.empty();
    }

    user.setUsername(normalizedUsername);
    user.setEmail(normalizedEmail);
    user.setPhone(normalizedPhone);
    return Optional.of(userRepository.update(user));
  }

  public Optional<UserEntity> resetPassword(Long id, String password) {
    Optional<UserEntity> existing = userRepository.findByIdAndDeletedNotTrue(id);
    if (existing.isEmpty()) {
      return Optional.empty();
    }
    String normalizedPassword = normalize(password);
    if (normalizedPassword == null) {
      return Optional.empty();
    }
    UserEntity user = existing.get();
    user.setPassword(passwordEncoder.encode(normalizedPassword));
    return Optional.of(userRepository.update(user));
  }

  public boolean deleteUser(Long id) {
    Optional<UserEntity> existing = userRepository.findByIdAndDeletedNotTrue(id);
    if (existing.isEmpty()) {
      return false;
    }
    UserEntity user = existing.get();
    user.setDeleted(Boolean.TRUE);
    user.setDeletedAt(LocalDateTime.now());
    userRepository.update(user);
    return true;
  }

  private boolean isUniqueForCreate(String username, String email, String phone) {
    if (userRepository.findByUsername(username).filter(u -> !Boolean.TRUE.equals(u.getDeleted())).isPresent()) {
      return false;
    }
    if (email != null && userRepository.findByEmail(email).filter(u -> !Boolean.TRUE.equals(u.getDeleted())).isPresent()) {
      return false;
    }
    if (phone != null && userRepository.findByPhone(phone).filter(u -> !Boolean.TRUE.equals(u.getDeleted())).isPresent()) {
      return false;
    }
    return true;
  }

  private boolean isUniqueForUpdate(Long id, String username, String email, String phone) {
    if (userRepository.findByUsername(username)
        .filter(u -> !u.getId().equals(id) && !Boolean.TRUE.equals(u.getDeleted()))
        .isPresent()) {
      return false;
    }
    if (email != null && userRepository.findByEmail(email)
        .filter(u -> !u.getId().equals(id) && !Boolean.TRUE.equals(u.getDeleted()))
        .isPresent()) {
      return false;
    }
    if (phone != null && userRepository.findByPhone(phone)
        .filter(u -> !u.getId().equals(id) && !Boolean.TRUE.equals(u.getDeleted()))
        .isPresent()) {
      return false;
    }
    return true;
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
