package io.vacivor.nexo.security.web.session;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.vacivor.nexo.security.web.session.events.SessionCreatedEvent;
import io.vacivor.nexo.security.web.session.events.SessionDeletedEvent;
import io.vacivor.nexo.security.web.session.events.SessionExpiredEvent;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.Optional;

/**
 * @author lumreco lumreco@gmail.com
 */
@Singleton
@Requires(beans = SessionRepository.class)
public class SessionManager<S extends Session> {

  private final SessionRepository<S> sessionRepository;
  private final SessionIdGenerator sessionIdGenerator;
  private final ApplicationEventPublisher<Object> eventPublisher;

  public SessionManager(SessionRepository<S> sessionRepository,
      SessionIdGenerator sessionIdGenerator,
      ApplicationEventPublisher<Object> eventPublisher) {
    this.sessionRepository = sessionRepository;
    this.sessionIdGenerator = sessionIdGenerator;
    this.eventPublisher = eventPublisher;
  }

  public S createSession() {
    String id = sessionIdGenerator.generateId();
    S session = sessionRepository.createSession(id);
    sessionRepository.save(session);
    eventPublisher.publishEvent(new SessionCreatedEvent(this, session));
    return session;
  }

  public Optional<S> findById(String id) {
    Optional<S> session = sessionRepository.findById(id);
    if (session.isEmpty()) {
      return Optional.empty();
    }
    S found = session.get();
    if (found.isExpired()) {
      sessionRepository.delete(found);
      eventPublisher.publishEvent(new SessionExpiredEvent(this, found));
      return Optional.empty();
    }
    found.setLastAccessedTime(Instant.now());
    found.setNew(false);
    sessionRepository.save(found);
    return Optional.of(found);
  }

  public void save(S session) {
    sessionRepository.save(session);
  }

  public void deleteById(String id) {
    sessionRepository.findById(id).ifPresent(session -> {
      sessionRepository.delete(session);
      eventPublisher.publishEvent(new SessionDeletedEvent(this, session));
    });
  }
}
