package io.vacivor.nexo.oidc.endpoints;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.vacivor.nexo.oidc.OidcJwksService;
import io.vacivor.nexo.oidc.OidcService;
import java.util.Map;

@Controller
public class OidcDiscoveryEndpoint {

  private final OidcService oidcService;
  private final OidcJwksService jwksService;

  public OidcDiscoveryEndpoint(OidcService oidcService, OidcJwksService jwksService) {
    this.oidcService = oidcService;
    this.jwksService = jwksService;
  }

  @Get("/.well-known/openid-configuration")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> discovery(HttpRequest<?> request) {
    return oidcService.discovery(request);
  }

  @Get("/oauth/jwks")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> jwks() {
    return jwksService.jwks();
  }
}
