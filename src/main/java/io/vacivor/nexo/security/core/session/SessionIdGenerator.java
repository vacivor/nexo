package io.vacivor.nexo.security.core.session;

public interface SessionIdGenerator {

  /**
   * Generates a new session ID.
   *
   * @return the generated session ID
   */
  String generateId();
}
