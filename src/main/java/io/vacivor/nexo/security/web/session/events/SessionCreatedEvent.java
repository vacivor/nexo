package io.vacivor.nexo.security.web.session.events;


import io.vacivor.nexo.security.web.session.Session;


public class SessionCreatedEvent extends AbstractSessionEvent {

  public SessionCreatedEvent(Object source, Session session) {
    super(source, session);
  }
}
