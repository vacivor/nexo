package io.vacivor.nexo.security;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.vacivor.nexo.security.auth.AuthenticationService;
import io.vacivor.nexo.security.auth.LoginRequest;
import io.vacivor.nexo.security.auth.LoginResponse;
import io.vacivor.nexo.security.auth.RegisterRequest;
import io.vacivor.nexo.security.auth.RegisterResponse;
import io.vacivor.nexo.security.auth.RegistrationService;

@Controller
public class LoginController {

  private final AuthenticationService authenticationService;
  private final RegistrationService registrationService;

  public LoginController(AuthenticationService authenticationService,
      RegistrationService registrationService) {
    this.authenticationService = authenticationService;
    this.registrationService = registrationService;
  }

  @Post(uri = "/login", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  public HttpResponse<LoginResponse> login(@Body LoginRequest request) {
    return authenticationService.login(request.getIdentifier(), request.getPassword());
  }

  @Post(uri = "/register", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  public HttpResponse<RegisterResponse> register(@Body RegisterRequest request) {
    return registrationService.register(request);
  }
}
