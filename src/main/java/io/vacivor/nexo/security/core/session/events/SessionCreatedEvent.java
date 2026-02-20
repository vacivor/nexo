package io.vacivor.nexo.security.core.session.events;


import io.vacivor.nexo.security.core.session.Session;


public class SessionCreatedEvent extends AbstractSessionEvent {

  public SessionCreatedEvent(Object source, Session session) {
    super(source, session);
  }
}
