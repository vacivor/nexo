package io.vacivor.nexo.oidc.endpoints;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.MediaType;
import io.vacivor.nexo.security.oidc.OidcConfiguration;
import io.vacivor.nexo.security.oidc.OidcJwtSigner;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OIDC discovery and JWKS endpoints.
 */
@Controller
public class OidcDiscoveryEndpoint {

  private final OidcConfiguration configuration;
  private final OidcJwtSigner jwtSigner;

  public OidcDiscoveryEndpoint(OidcConfiguration configuration, OidcJwtSigner jwtSigner) {
    this.configuration = configuration;
    this.jwtSigner = jwtSigner;
  }

  @Get(value = "/.well-known/openid-configuration", produces = MediaType.APPLICATION_JSON)
  public HttpResponse<Map<String, Object>> getConfiguration(HttpRequest<?> request) {
    String issuer = configuration.getIssuer();
    Map<String, Object> config = new HashMap<>();
    config.put("issuer", issuer);
    config.put("authorization_endpoint", issuer + "/oidc/authorize");
    config.put("token_endpoint", issuer + "/oidc/token");
    config.put("jwks_uri", issuer + "/.well-known/jwks.json");
    config.put("userinfo_endpoint", issuer + "/oidc/userinfo");
    config.put("response_types_supported", List.of("code"));
    config.put("subject_types_supported", List.of("public"));
    config.put("id_token_signing_alg_values_supported", List.of("HS256"));
    config.put("scopes_supported", List.of("openid", "profile", "email"));
    config.put("token_endpoint_auth_methods_supported", List.of("client_secret_basic", "client_secret_post"));
    config.put("grant_types_supported", List.of("authorization_code"));
    config.put("code_challenge_methods_supported", List.of("S256"));
    config.put("claims_supported", List.of("sub", "iss", "aud", "exp", "iat"));
    return HttpResponse.ok(config);
  }

  @Get(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON)
  public HttpResponse<Map<String, Object>> getJsonWebKeySet() {
    String k = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(configuration.getHmacSecret().getBytes());
    Map<String, Object> key = new HashMap<>();
    key.put("kty", "oct");
    key.put("kid", jwtSigner.keyId(configuration.getHmacSecret()));
    key.put("alg", "HS256");
    key.put("k", k);
    Map<String, Object> jwks = new HashMap<>();
    jwks.put("keys", List.of(key));
    return HttpResponse.ok(jwks);
  }
}
