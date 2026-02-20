package io.vacivor.nexo.security.endpoints;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.vacivor.nexo.security.auth.service.AuthenticationService;
import io.vacivor.nexo.security.auth.dto.LoginResponse;
import io.vacivor.nexo.security.auth.dto.OAuth2LoginRequest;
import io.vacivor.nexo.security.auth.dto.OidcLoginRequest;
import io.vacivor.nexo.security.auth.dto.SocialLoginRequest;

@Controller
public class ExternalLoginEndpoint {

  private final AuthenticationService authenticationService;

  public ExternalLoginEndpoint(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @Post(uri = "/login/oidc", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  public HttpResponse<LoginResponse> loginWithOidc(@Body OidcLoginRequest request) {
    return authenticationService.loginWithOidc(request.getProvider(), request.getIdToken());
  }

  @Post(uri = "/login/social", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  public HttpResponse<LoginResponse> loginWithSocial(@Body SocialLoginRequest request) {
    return authenticationService.loginWithSocial(request.getProvider(), request.getAccessToken());
  }

  @Post(uri = "/login/oauth2", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  public HttpResponse<LoginResponse> loginWithOAuth2(@Body OAuth2LoginRequest request) {
    return authenticationService.loginWithOAuth2Code(
        request.getProvider(), request.getCode(), request.getRedirectUri());
  }
}
