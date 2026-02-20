package io.vacivor.nexo.admin.session;

import io.vacivor.nexo.security.auth.Authentication;
import io.vacivor.nexo.security.auth.AuthenticationSessionCodec;
import io.vacivor.nexo.security.auth.AuthenticationSessionService;
import io.vacivor.nexo.security.web.session.Session;
import io.vacivor.nexo.security.web.session.SessionRepository;
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
}
