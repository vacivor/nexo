package io.vacivor.nexo.security.web.session;

import java.util.UUID;
import jakarta.inject.Singleton;

/**
 * @author lumreco lumreco@gmail.com
 */
@Singleton
public class UuidSessionIdGenerator implements SessionIdGenerator {

  private static final UuidSessionIdGenerator INSTANCE = new UuidSessionIdGenerator();

  @Override
  public String generateId() {
    return UUID.randomUUID().toString();
  }

  public static UuidSessionIdGenerator getInstance() {
    return INSTANCE;
  }
}
