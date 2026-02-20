package io.vacivor.nexo.oidc.endpoints;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.vacivor.nexo.authorizationserver.oidc.OidcDiscoveryService;
import io.vacivor.nexo.oidc.OidcJwksService;
import java.util.Map;

@Controller
public class OidcDiscoveryEndpoint {

  private final OidcDiscoveryService oidcDiscoveryService;
  private final OidcJwksService jwksService;

  public OidcDiscoveryEndpoint(OidcDiscoveryService oidcDiscoveryService, OidcJwksService jwksService) {
    this.oidcDiscoveryService = oidcDiscoveryService;
    this.jwksService = jwksService;
  }

  @Get("/.well-known/openid-configuration")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> discovery() {
    return oidcDiscoveryService.discovery();
  }

  @Get("/oidc/jwks")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> jwks() {
    return jwksService.jwks();
  }
}
