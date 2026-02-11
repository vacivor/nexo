package io.vacivor.nexo.security.web.session;

import java.util.Optional;

/**
 * @author lumreco lumreco@gmail.com
 */
public interface SessionRepository<S extends Session> {

  S createSession(String id);

  Optional<S> findById(String id);

  S save(S session);

  void deleteById(String id);

  default void delete(S session) {
    deleteById(session.getId());
  }
}
