package io.vacivor.nexo.security.core.session.events;

import io.vacivor.nexo.security.core.session.Session;


public class SessionDestroyedEvent extends AbstractSessionEvent {

  public SessionDestroyedEvent(Object source, Session session) {
    super(source, session);
  }
}
