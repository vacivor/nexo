package io.vacivor.nexo.crypto;

import io.vacivor.nexo.crypto.password.PasswordEncoder;
import jakarta.inject.Singleton;
import java.security.SecureRandom;

/**
 * Implementation of PasswordEncoder that uses the BCrypt strong hashing function. Clients can
 * optionally supply a "strength" (a.k.a. log rounds in BCrypt) and a SecureRandom instance. The
 * larger the strength parameter, the more work will have to be done (exponentially) to hash the
 * passwords. The default value is 10.
 *
 * <p>Important: This implementation currently only supports BCrypt 2a version (format "$2a$").
 * Other versions like 2y or 2b are not supported. The resulting hash will always start with
 * "$2a$".
 *
 *
 */
@Singleton
public class BCryptPasswordEncoder implements PasswordEncoder {

  private final int strength;
  private final SecureRandom random;

  /**
   * Creates a BCryptPasswordEncoder with default strength (10)
   */
  public BCryptPasswordEncoder() {
    this(10);
  }

  /**
   * Creates a BCryptPasswordEncoder with the given strength
   *
   * @param strength the log rounds to use, between 4 and 31
   */
  public BCryptPasswordEncoder(int strength) {
    this(strength, null);
  }

  /**
   * Creates a BCryptPasswordEncoder with the given strength and SecureRandom instance
   *
   * @param strength the log rounds to use, between 4 and 31
   * @param random   the secure random instance to use, can be null
   */
  public BCryptPasswordEncoder(int strength, SecureRandom random) {
    if (strength < 4 || strength > 31) {
      throw new IllegalArgumentException("Strength must be between 4 and 31");
    }
    this.strength = strength;
    this.random = random;
  }

  @Override
  public String encode(CharSequence rawPassword) {
    if (rawPassword == null) {
      throw new IllegalArgumentException("Raw password cannot be null");
    }
    String salt = genSalt();
    return BCrypt.hashpw(rawPassword.toString(), salt);
  }

  private String genSalt() {
    if (this.random != null) {
      return BCrypt.gensalt(this.strength, this.random);
    }
    return BCrypt.gensalt(this.strength);
  }

  @Override
  public boolean matches(CharSequence rawPassword, String encodedPassword) {
    if (rawPassword == null) {
      throw new IllegalArgumentException("Raw password cannot be null");
    }
    if (encodedPassword == null || encodedPassword.isEmpty()) {
      return false;
    }
    return BCrypt.checkpw(rawPassword.toString(), encodedPassword);
  }
}
