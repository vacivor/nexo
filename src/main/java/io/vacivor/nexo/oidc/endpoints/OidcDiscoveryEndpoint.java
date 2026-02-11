package io.vacivor.nexo.oidc.endpoints;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OIDC discovery and JWKS endpoints.
 */
@Controller
public class OidcDiscoveryEndpoint {

  @Get("/.well-known/openid-configuration")
  public HttpResponse<Map<String, Object>> getConfiguration() {
    String issuer = "issuer";
    Map<String, Object> config = new HashMap<>();
    config.put("issuer", issuer);
    config.put("authorization_endpoint", issuer + "/oauth2/authorize");
    config.put("token_endpoint", issuer + "/oauth2/token");
    config.put("jwks_uri", issuer + "/oidc/jwks");
    config.put("userinfo_endpoint", issuer + "/oidc/userinfo");
    config.put("response_types_supported", List.of("code"));
    config.put("subject_types_supported", List.of("public"));
    config.put("id_token_signing_alg_values_supported", List.of("RS256"));
    config.put("scopes_supported", List.of("openid", "profile", "email"));
    config.put("token_endpoint_auth_methods_supported", List.of("client_secret_basic", "client_secret_post"));
    config.put("grant_types_supported", List.of("authorization_code", "refresh_token"));
    config.put("code_challenge_methods_supported", List.of("S256"));
    config.put("revocation_endpoint", issuer + "/oauth2/revoke");
    config.put("claims_supported", List.of("sub", "iss", "aud", "exp", "iat", "tenant_id"));
    return HttpResponse.ok(config);
  }

  @Get("/.well-known/jwks.json")
  public HttpResponse<String> getJsonWebKeySet() {
    return HttpResponse.ok(getJwksJson());
  }

  public String getJwksJson() {
    String k = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(Base64.getDecoder().decode("getHmacSecret()"));
    return "{\"keys\":[{\"kty\":\"oct\",\"kid\":\"" + "config.getKeyId()"
        + "\",\"alg\":\"HS256\",\"k\":\"" + k + "\"}]}";
  }
}
