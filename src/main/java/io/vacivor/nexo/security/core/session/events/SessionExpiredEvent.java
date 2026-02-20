package io.vacivor.nexo.security.core.session.events;

import io.vacivor.nexo.security.core.session.Session;


public class SessionExpiredEvent extends AbstractSessionEvent {

  public SessionExpiredEvent(Object source, Session session) {
    super(source, session);
  }
}
