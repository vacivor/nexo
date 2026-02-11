package io.vacivor.nexo.security.web.session.events;

import io.vacivor.nexo.security.web.session.Session;

/**
 * @author lumreco lumreco@gmail.com
 */
public class SessionDeletedEvent extends AbstractSessionEvent {

  public SessionDeletedEvent(Object source, Session session) {
    super(source, session);
  }
}
