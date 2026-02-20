package io.vacivor.nexo.security.auth.core;

import io.vacivor.nexo.security.auth.core.*;
import io.vacivor.nexo.security.auth.service.*;
import io.vacivor.nexo.security.auth.persistence.*;
import io.vacivor.nexo.security.auth.handler.*;
import io.vacivor.nexo.security.auth.password.*;
import io.vacivor.nexo.security.auth.provider.local.*;
import io.vacivor.nexo.security.auth.client.*;
import io.vacivor.nexo.security.auth.dto.*;
import io.vacivor.nexo.security.auth.web.*;

import io.micronaut.http.HttpRequest;
import java.util.Optional;

public final class SecurityContext {

  public static final String AUTHENTICATION_ATTRIBUTE = SecurityContext.class.getName() + ".AUTHENTICATION";

  private SecurityContext() {
  }

  public static Optional<Authentication> getAuthentication(HttpRequest<?> request) {
    return request.getAttribute(AUTHENTICATION_ATTRIBUTE, Authentication.class);
  }

  public static void setAuthentication(HttpRequest<?> request, Authentication authentication) {
    if (authentication == null) {
      return;
    }
    request.setAttribute(AUTHENTICATION_ATTRIBUTE, authentication);
  }
}