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

import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Singleton
public class AuthenticationManager {

  private final List<AuthenticationProvider<?>> providers;

  public AuthenticationManager(List<AuthenticationProvider<?>> providers) {
    this.providers = providers;
  }

  public Optional<Authentication> authenticate(AuthenticationToken token) {
    for (AuthenticationProvider<?> provider : providers) {
      if (provider.supports(token)) {
        @SuppressWarnings("unchecked")
        AuthenticationProvider<AuthenticationToken> typed = (AuthenticationProvider<AuthenticationToken>) provider;
        Optional<Authentication> authentication = typed.authenticate(token);
        if (authentication.isPresent()) {
          return authentication;
        }
      }
    }
    return Optional.empty();
  }
}