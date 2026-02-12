package io.vacivor.nexo.security.oidc;

import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Singleton
public class OidcJwtSigner {

  private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
  private final ObjectMapper objectMapper;

  public OidcJwtSigner(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public String signHs256(String secret, Map<String, Object> claims) {
    try {
      Map<String, Object> header = new LinkedHashMap<>();
      header.put("alg", "HS256");
      header.put("typ", "JWT");
      String headerJson = objectMapper.writeValueAsString(header);
      String payloadJson = objectMapper.writeValueAsString(claims);
      String headerPart = URL_ENCODER.encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
      String payloadPart = URL_ENCODER.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
      String signingInput = headerPart + "." + payloadPart;
      byte[] signature = hmacSha256(secret, signingInput);
      String signaturePart = URL_ENCODER.encodeToString(signature);
      return signingInput + "." + signaturePart;
    } catch (Exception e) {
      throw new IllegalStateException("Failed to sign JWT", e);
    }
  }

  public String keyId(String secret) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
      return URL_ENCODER.encodeToString(hash).substring(0, 8);
    } catch (Exception e) {
      return "default";
    }
  }

  public Map<String, Object> buildIdTokenClaims(String issuer, String subject, String audience,
      Instant now, Instant expiresAt, String nonce) {
    Map<String, Object> claims = new LinkedHashMap<>();
    claims.put("iss", issuer);
    claims.put("sub", subject);
    claims.put("aud", audience);
    claims.put("iat", now.getEpochSecond());
    claims.put("exp", expiresAt.getEpochSecond());
    if (nonce != null) {
      claims.put("nonce", nonce);
    }
    return claims;
  }

  private byte[] hmacSha256(String secret, String content) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
  }
}
