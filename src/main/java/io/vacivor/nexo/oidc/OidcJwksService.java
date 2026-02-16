package io.vacivor.nexo.oidc;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.inject.Singleton;

@Singleton
public class OidcJwksService {

  private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
  private final OidcKeyService keyService;

  public OidcJwksService(OidcKeyService keyService) {
    this.keyService = keyService;
  }

  public Map<String, Object> jwks() {
    if (!keyService.isRsa()) {
      return Map.of("keys", List.of());
    }
    RSAPublicKey rsa = (RSAPublicKey) keyService.getPublicKey();
    Map<String, Object> key = new LinkedHashMap<>();
    key.put("kty", "RSA");
    key.put("use", "sig");
    key.put("alg", "RS256");
    key.put("kid", keyService.getKeyId());
    key.put("n", URL_ENCODER.encodeToString(rsa.getModulus().toByteArray()));
    key.put("e", URL_ENCODER.encodeToString(rsa.getPublicExponent().toByteArray()));
    return Map.of("keys", List.of(key));
  }
}
