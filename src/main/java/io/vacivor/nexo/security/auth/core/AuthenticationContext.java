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

import io.micronaut.runtime.http.scope.RequestScope;
import io.micronaut.http.context.ServerRequestContext;
import java.util.Optional;

@RequestScope
public class AuthenticationContext {

  public Optional<Authentication> getAuthentication() {
    return ServerRequestContext.currentRequest()
        .flatMap(SecurityContext::getAuthentication);
  }
}