package io.vacivor.nexo.security.core.session.events;

import io.vacivor.nexo.security.core.session.Session;


public class SessionDeletedEvent extends AbstractSessionEvent {

  public SessionDeletedEvent(Object source, Session session) {
    super(source, session);
  }
}
