package io.vacivor.nexo.security.web.session;

public interface SessionIdGenerator {

  /**
   * Generates a new session ID.
   *
   * @return the generated session ID
   */
  String generateId();
}
