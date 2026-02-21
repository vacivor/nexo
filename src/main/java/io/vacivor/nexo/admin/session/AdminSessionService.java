package io.vacivor.nexo.admin.session;

import io.vacivor.nexo.security.auth.core.Authentication;
import io.vacivor.nexo.security.auth.service.AuthenticationSessionCodec;
import io.vacivor.nexo.security.auth.service.AuthenticationSessionService;
import io.vacivor.nexo.security.core.session.SessionCursor;
import io.vacivor.nexo.security.core.session.Session;
import io.vacivor.nexo.security.core.session.SessionRepository;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Singleton
public class AdminSessionService {

  private final SessionRepository<? extends Session> sessionRepository;
  private final AuthenticationSessionCodec authenticationSessionCodec;

  public AdminSessionService(SessionRepository<? extends Session> sessionRepository,
      AuthenticationSessionCodec authenticationSessionCodec) {
    this.sessionRepository = sessionRepository;
    this.authenticationSessionCodec = authenticationSessionCodec;
  }

  public List<SessionView> listSessions(int offset, int limit) {
    return sessionRepository.findSessions(offset, limit).stream()
        .map(this::toView)
        .toList();
  }

  public SessionPage listSessionsByCursor(String cursor, int limit) {
    List<? extends Session> pageWithExtra = sessionRepository.findSessionsByCursor(cursor, limit + 1);
    boolean hasMore = pageWithExtra.size() > limit;
    List<? extends Session> page = hasMore ? pageWithExtra.subList(0, limit) : pageWithExtra;
    String nextCursor = null;
    if (hasMore && !page.isEmpty()) {
      nextCursor = SessionCursor.encode(page.get(page.size() - 1));
    }
    List<SessionView> items = page.stream().map(this::toView).toList();
    return new SessionPage(items, nextCursor, hasMore);
  }

  public long countSessions() {
    return sessionRepository.countSessions();
  }

  public boolean deleteSession(String sessionId) {
    Optional<? extends Session> existing = sessionRepository.findById(sessionId);
    if (existing.isEmpty()) {
      return false;
    }
    sessionRepository.deleteById(sessionId);
    return true;
  }

  private SessionView toView(Session session) {
    Instant expiresAt = session.getMaxInactiveInterval()
        .map(duration -> session.getLastAccessedTime().plus(duration))
        .orElse(null);
    String principal = resolvePrincipal(session);
    return new SessionView(
        session.getId(),
        principal,
        session.getCreationTime(),
        session.getLastAccessedTime(),
        expiresAt,
        session.isNew());
  }

  private String resolvePrincipal(Session session) {
    Object raw = session.getAttribute(AuthenticationSessionService.authenticationAttributeName());
    if (raw == null) {
      return null;
    }
    return authenticationSessionCodec.fromSessionValue(raw)
        .map(Authentication::getPrincipal)
        .map(String::valueOf)
        .orElse(null);
  }

  @Serdeable
  public record SessionView(
      String id,
      String principal,
      Instant createdAt,
      Instant lastAccessedAt,
      Instant expiresAt,
      boolean isNew) {
  }

  @Serdeable
  public record SessionPage(
      List<SessionView> items,
      String nextCursor,
      boolean hasMore) {
  }
}
