package io.vacivor.nexo.client;

import jakarta.inject.Singleton;
import java.security.SecureRandom;

@Singleton
public class Base62ClientIdGenerator implements ClientIdGenerator {

  private static final char[] BASE62 =
      "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
  private static final int CLIENT_ID_LENGTH = 27;
  private final SecureRandom secureRandom = new SecureRandom();

  @Override
  public String generate() {
    char[] value = new char[CLIENT_ID_LENGTH];
    for (int i = 0; i < CLIENT_ID_LENGTH; i++) {
      value[i] = BASE62[secureRandom.nextInt(BASE62.length)];
    }
    return new String(value);
  }
}
