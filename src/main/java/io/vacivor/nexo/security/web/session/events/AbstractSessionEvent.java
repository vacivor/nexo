package io.vacivor.nexo.security.web.session.events;

import io.micronaut.context.event.ApplicationEvent;
import io.vacivor.nexo.security.web.session.Session;

public abstract class AbstractSessionEvent extends ApplicationEvent {

  private final String sessionId;

  private final Session session;

  public AbstractSessionEvent(Object source, Session session) {
    super(source);
    this.sessionId = session.getId();
    this.session = session;
  }

  public <S extends Session> S getSession() {
    return (S) this.session;
  }

  public String getSessionId() {
    return this.sessionId;
  }
}
