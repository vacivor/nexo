package io.vacivor.nexo.security.auth.web;

import io.vacivor.nexo.security.auth.core.*;
import io.vacivor.nexo.security.auth.service.*;
import io.vacivor.nexo.security.auth.persistence.*;
import io.vacivor.nexo.security.auth.handler.*;
import io.vacivor.nexo.security.auth.password.*;
import io.vacivor.nexo.security.auth.provider.local.*;
import io.vacivor.nexo.security.auth.client.*;
import io.vacivor.nexo.security.auth.dto.*;
import io.vacivor.nexo.security.auth.web.*;

import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class AuthenticationArgumentBinder implements TypedRequestArgumentBinder<Authentication> {

  @Override
  public Argument<Authentication> argumentType() {
    return Argument.of(Authentication.class);
  }

  @Override
  public ArgumentBinder.BindingResult<Authentication> bind(ArgumentConversionContext<Authentication> context,
      HttpRequest<?> source) {
    Optional<Authentication> authentication = SecurityContext.getAuthentication(source);
    return () -> authentication;
  }
}