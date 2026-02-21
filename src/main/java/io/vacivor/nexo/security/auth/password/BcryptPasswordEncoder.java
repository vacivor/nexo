package io.vacivor.nexo.security.auth.password;

import io.vacivor.nexo.crypto.BCrypt;

public class BcryptPasswordEncoder implements PasswordEncoder {

  @Override
  public String encode(String rawPassword) {
    if (rawPassword == null) {
      return null;
    }
    return BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
  }

  @Override
  public boolean matches(String rawPassword, String encodedPassword) {
    if (rawPassword == null || encodedPassword == null) {
      return false;
    }
    return BCrypt.checkpw(rawPassword, encodedPassword);
  }
}
