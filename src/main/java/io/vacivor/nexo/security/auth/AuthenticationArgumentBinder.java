package io.vacivor.nexo.security.auth;

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
