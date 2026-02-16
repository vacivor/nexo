package io.vacivor.nexo.oidc.endpoints;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.vacivor.nexo.oidc.OidcAccessToken;
import io.vacivor.nexo.oidc.OidcService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class OidcUserInfoEndpoint {

  private final OidcService oidcService;

  public OidcUserInfoEndpoint(OidcService oidcService) {
    this.oidcService = oidcService;
  }

  @Get(value = "/oidc/userinfo", produces = MediaType.APPLICATION_JSON)
  public HttpResponse<?> userInfo(HttpRequest<?> request) {
    String authHeader = request.getHeaders().get("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return HttpResponse.status(HttpStatus.UNAUTHORIZED);
    }
    String token = authHeader.substring("Bearer ".length()).trim();
    Optional<OidcAccessToken> accessToken = oidcService.findAccessToken(token);
    if (accessToken.isEmpty()) {
      return HttpResponse.status(HttpStatus.UNAUTHORIZED);
    }
    String subject = accessToken.get().getSubject();
    Map<String, Object> response = new HashMap<>();
    response.put("sub", subject);
    oidcService.findUserByUsername(subject).ifPresent(user -> {
      response.put("email", user.getEmail());
      response.put("phone_number", user.getPhone());
      response.put("preferred_username", user.getUsername());
    });
    return HttpResponse.ok(response);
  }
}
