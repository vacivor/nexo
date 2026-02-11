package io.vacivor.nexo.security.auth;

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
