package io.vacivor.nexo.security.auth;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.vacivor.nexo.security.auth.oauth2.GenericOAuth2Authentication;
import io.vacivor.nexo.security.auth.oauth2.GithubOAuth2Authentication;
import io.vacivor.nexo.security.auth.oauth2.OAuth2Authentication;
import io.vacivor.nexo.security.auth.oidc.OidcAuthenticationToken;
import io.vacivor.nexo.security.auth.social.SocialAuthenticationToken;
import io.vacivor.nexo.security.providers.IdentityProviderProtocol;
import io.vacivor.nexo.security.providers.IdentityProviderService;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class AuthenticationService {

  private final AuthenticationManager authenticationManager;
  private final AuthenticationSuccessHandler successHandler;
  private final AuthenticationFailureHandler failureHandler;
  private final AuthenticationPersistence authenticationPersistence;
  private final IdentityProviderService identityProviderService;

  public AuthenticationService(AuthenticationManager authenticationManager,
      AuthenticationSuccessHandler successHandler,
      AuthenticationFailureHandler failureHandler,
      AuthenticationPersistence authenticationPersistence,
      IdentityProviderService identityProviderService) {
    this.authenticationManager = authenticationManager;
    this.successHandler = successHandler;
    this.failureHandler = failureHandler;
    this.authenticationPersistence = authenticationPersistence;
    this.identityProviderService = identityProviderService;
  }

  public Optional<Authentication> authenticate(String identifier, String password) {
    IdentifierPasswordAuthenticationToken token = new IdentifierPasswordAuthenticationToken(identifier, password);
    return authenticate(token);
  }

  public Optional<Authentication> authenticate(AuthenticationToken token) {
    return authenticationManager.authenticate(token);
  }

  public MutableHttpResponse<LoginResponse> login(String identifier, String password) {
    IdentifierPasswordAuthenticationToken token = new IdentifierPasswordAuthenticationToken(identifier, password);
    return login(token);
  }

  public MutableHttpResponse<LoginResponse> loginWithOidc(String provider, String idToken) {
    if (!isProviderEnabled(IdentityProviderProtocol.OIDC, provider)) {
      return unauthorized();
    }
    return login(new OidcAuthenticationToken(provider, idToken));
  }

  public MutableHttpResponse<LoginResponse> loginWithSocial(String provider, String accessToken) {
    if (!isProviderEnabled(IdentityProviderProtocol.SOCIAL, provider)) {
      return unauthorized();
    }
    return login(new SocialAuthenticationToken(provider, accessToken));
  }

  public MutableHttpResponse<LoginResponse> loginWithOAuth2Code(String provider, String code, String redirectUri) {
    if (!isProviderEnabled(IdentityProviderProtocol.OAUTH2, provider)) {
      return unauthorized();
    }
    return login(toOAuth2Authentication(provider, code, redirectUri));
  }

  public MutableHttpResponse<LoginResponse> login(AuthenticationToken token) {
    Optional<Authentication> authentication = authenticate(token);
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

  private boolean isProviderEnabled(IdentityProviderProtocol protocol, String provider) {
    if (provider == null || provider.isBlank()) {
      return false;
    }
    return identityProviderService.resolveEnabled(protocol, provider.trim()).isPresent();
  }

  private MutableHttpResponse<LoginResponse> unauthorized() {
    MutableHttpResponse<LoginResponse> response = HttpResponse.unauthorized();
    failureHandler.onFailure(response);
    authenticationPersistence.onFailure(response);
    return response;
  }

  private OAuth2Authentication toOAuth2Authentication(String provider, String code, String redirectUri) {
    if (provider != null && GithubOAuth2Authentication.PROVIDER.equalsIgnoreCase(provider.trim())) {
      return new GithubOAuth2Authentication(code, redirectUri);
    }
    return new GenericOAuth2Authentication(provider, code, redirectUri);
  }
}
