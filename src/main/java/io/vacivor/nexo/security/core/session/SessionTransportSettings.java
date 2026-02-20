package io.vacivor.nexo.security.core.session;

public interface SessionTransportSettings {

  String getCookieName();

  String getHeaderName();

  boolean isCookieTransportEnabled();

  boolean isHeaderTransportEnabled();

  boolean isCookieSecure();

  String getCookieSameSite();
}
