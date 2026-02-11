package io.vacivor.nexo.security.auth.user;

import io.vacivor.nexo.security.user.UserEntity;
import io.vacivor.nexo.security.user.UserRepository;
import jakarta.inject.Singleton;
import java.util.Optional;
import java.util.regex.Pattern;

@Singleton
public class IdentityResolverImpl implements IdentityResolver {

  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
  private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{6,20}$");

  private final UserRepository userRepository;

  public IdentityResolverImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Optional<UserDetails> resolve(String identifier) {
    if (identifier == null || identifier.isBlank()) {
      return Optional.empty();
    }
    String normalized = identifier.trim();
    Optional<UserEntity> user = findUser(normalized);
    return user.map(this::toUserDetails);
  }

  private Optional<UserEntity> findUser(String identifier) {
    if (EMAIL_PATTERN.matcher(identifier).matches()) {
      return userRepository.findByEmail(identifier);
    }
    if (PHONE_PATTERN.matcher(identifier).matches()) {
      return userRepository.findByPhone(identifier);
    }
    return userRepository.findByUsername(identifier);
  }

  private UserDetails toUserDetails(UserEntity user) {
    UserDetails details = new UserDetails();
    details.setUsername(user.getUsername());
    details.setPassword(user.getPassword());
    return details;
  }
}
