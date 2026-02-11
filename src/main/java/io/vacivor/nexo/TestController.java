package io.vacivor.nexo;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.vacivor.nexo.security.auth.Authentication;
import io.vacivor.nexo.security.auth.AuthenticationContext;
import java.util.Optional;

@Controller("test")
public class TestController {

  private final AuthenticationContext authenticationContext;

  public TestController(AuthenticationContext authenticationContext) {
    this.authenticationContext = authenticationContext;
  }

  @Get
  public Optional<Authentication> getAuthentication() {
    return authenticationContext.getAuthentication();
  }
}
