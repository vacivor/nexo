package io.vacivor.nexo.security.auth.password;

public class PlainTextPasswordEncoder implements PasswordEncoder {

  @Override
  public String encode(String rawPassword) {
    return rawPassword;
  }

  @Override
  public boolean matches(String rawPassword, String encodedPassword) {
    if (rawPassword == null || encodedPassword == null) {
      return false;
    }
    return encodedPassword.equals(rawPassword);
  }
}
