package io.vacivor.nexo.security.web.session;

import java.util.Optional;

/**
 * @author lumreco lumreco@gmail.com
 */
public interface SessionRepository {

  Session createSession(String id);

  Optional<Session> findById(String id);

  Session save(Session session);

  void deleteById(String id);

  default void delete(Session session) {
    deleteById(session.getId());
  }
}
