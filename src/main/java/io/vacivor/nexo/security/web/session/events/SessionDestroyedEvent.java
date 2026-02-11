package io.vacivor.nexo.security.web.session.events;

import io.vacivor.nexo.security.web.session.Session;

/**
 * @author lumreco lumreco@gmail.com
 */
public class SessionDestroyedEvent extends AbstractSessionEvent {

  public SessionDestroyedEvent(Object source, Session session) {
    super(source, session);
  }
}
