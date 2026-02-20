package io.vacivor.nexo.security.core.session;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author lumreco lumreco@gmail.com
 */
public interface SessionRepository<S extends Session> {

  S createSession(String id);

  Optional<S> findById(String id);

  S save(S session);

  void deleteById(String id);

  default List<S> findAllSessions() {
    return Collections.emptyList();
  }

  default List<S> findSessions(int offset, int limit) {
    if (limit <= 0) {
      return Collections.emptyList();
    }
    int normalizedOffset = Math.max(0, offset);
    List<S> all = findAllSessions();
    if (normalizedOffset >= all.size()) {
      return Collections.emptyList();
    }
    int end = Math.min(all.size(), normalizedOffset + limit);
    return all.subList(normalizedOffset, end);
  }

  default long countSessions() {
    return findAllSessions().size();
  }

  default void delete(S session) {
    deleteById(session.getId());
  }
}
