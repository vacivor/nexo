package io.vacivor.nexo.security.auth;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class AuthenticationService {

  private final AuthenticationManager authenticationManager;
  private final AuthenticationSuccessHandler successHandler;
  private final AuthenticationFailureHandler failureHandler;
  private final AuthenticationPersistence authenticationPersistence;

  public AuthenticationService(AuthenticationManager authenticationManager,
      AuthenticationSuccessHandler successHandler,
      AuthenticationFailureHandler failureHandler,
      AuthenticationPersistence authenticationPersistence) {
    this.authenticationManager = authenticationManager;
    this.successHandler = successHandler;
    this.failureHandler = failureHandler;
    this.authenticationPersistence = authenticationPersistence;
  }

  public Optional<Authentication> authenticate(String identifier, String password) {
    IdentifierPasswordAuthenticationToken token = new IdentifierPasswordAuthenticationToken(identifier, password);
    return authenticationManager.authenticate(token);
  }

  public MutableHttpResponse<LoginResponse> login(String identifier, String password) {
    Optional<Authentication> authentication = authenticate(identifier, password);
    if (authentication.isEmpty()) {
      MutableHttpResponse<LoginResponse> response = HttpResponse.unauthorized();
      failureHandler.onFailure(response);
      authenticationPersistence.onFailure(response);
      return response;
    }
    Authentication auth = authentication.get();
    MutableHttpResponse<LoginResponse> response = HttpResponse.ok(
        new LoginResponse(String.valueOf(auth.getPrincipal()), auth.getAuthorities()));
    authenticationPersistence.onSuccess(auth, response);
    successHandler.onSuccess(auth, response);
    return response;
  }
}
