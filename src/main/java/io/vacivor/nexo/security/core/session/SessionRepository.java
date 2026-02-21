package io.vacivor.nexo.security.core.session;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;
import java.util.ArrayList;

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

  default List<S> findSessionsByCursor(String cursor, int limit) {
    if (limit <= 0) {
      return Collections.emptyList();
    }
    Optional<SessionCursor.CursorValue> cursorValue = SessionCursor.decode(cursor);
    List<S> all = new ArrayList<>(findAllSessions());
    all.sort(sessionSortComparator());
    List<S> page = new ArrayList<>(limit);
    for (S session : all) {
      if (cursorValue.isPresent() && !SessionCursor.isAfterCursor(session, cursorValue.get())) {
        continue;
      }
      page.add(session);
      if (page.size() >= limit) {
        break;
      }
    }
    return page;
  }

  default long countSessions() {
    return findAllSessions().size();
  }

  default void delete(S session) {
    deleteById(session.getId());
  }

  private static <S extends Session> Comparator<S> sessionSortComparator() {
    return Comparator.<S, java.time.Instant>comparing(Session::getLastAccessedTime)
        .reversed()
        .thenComparing(Session::getId, Comparator.reverseOrder());
  }
}
