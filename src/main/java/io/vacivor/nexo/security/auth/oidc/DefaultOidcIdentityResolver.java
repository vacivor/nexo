package io.vacivor.nexo.security.auth.oidc;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.ObjectMapper;
import io.vacivor.nexo.oidc.OidcConfiguration;
import jakarta.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Singleton
@Requires(missingBeans = OidcIdentityResolver.class)
public class DefaultOidcIdentityResolver implements OidcIdentityResolver {

  private final OidcConfiguration oidcConfiguration;
  private final ObjectMapper objectMapper;

  public DefaultOidcIdentityResolver(OidcConfiguration oidcConfiguration, ObjectMapper objectMapper) {
    this.oidcConfiguration = oidcConfiguration;
    this.objectMapper = objectMapper;
  }

  @Override
  public Optional<OidcIdentity> resolve(OidcAuthenticationToken token) {
    if (token.getIdToken() == null || token.getIdToken().isBlank()) {
      return Optional.empty();
    }
    String[] parts = token.getIdToken().split("\\.");
    if (parts.length != 3) {
      return Optional.empty();
    }
    if (!verifyHs256(parts[0], parts[1], parts[2], oidcConfiguration.getHmacSecret())) {
      return Optional.empty();
    }

    Map<String, Object> claims = parsePayload(parts[1]);
    if (claims.isEmpty()) {
      return Optional.empty();
    }
    if (!isNotExpired(claims.get("exp"))) {
      return Optional.empty();
    }

    Object subject = claims.get("sub");
    if (subject == null || String.valueOf(subject).isBlank()) {
      return Optional.empty();
    }

    OidcIdentity identity = new OidcIdentity();
    identity.setProvider(token.getProvider() == null || token.getProvider().isBlank()
        ? "default"
        : token.getProvider());
    identity.setSubject(String.valueOf(subject));
    identity.setEmail(stringClaim(claims, "email"));
    identity.setPhone(stringClaim(claims, "phone_number"));
    identity.setUsername(stringClaim(claims, "preferred_username"));
    return Optional.of(identity);
  }

  private Map<String, Object> parsePayload(String payloadPart) {
    try {
      byte[] json = Base64.getUrlDecoder().decode(payloadPart);
      return objectMapper.readValue(json, Argument.mapOf(String.class, Object.class));
    } catch (Exception e) {
      return Map.of();
    }
  }

  private boolean verifyHs256(String headerPart, String payloadPart, String signaturePart, String secret) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] actual = mac.doFinal((headerPart + "." + payloadPart).getBytes(StandardCharsets.UTF_8));
      String expected = Base64.getUrlEncoder().withoutPadding().encodeToString(actual);
      return constantTimeEquals(expected, signaturePart);
    } catch (Exception e) {
      return false;
    }
  }

  private boolean isNotExpired(Object expClaim) {
    if (expClaim == null) {
      return false;
    }
    long exp;
    if (expClaim instanceof Number n) {
      exp = n.longValue();
    } else {
      try {
        exp = Long.parseLong(String.valueOf(expClaim));
      } catch (NumberFormatException e) {
        return false;
      }
    }
    return Instant.now().getEpochSecond() < exp;
  }

  private String stringClaim(Map<String, Object> claims, String key) {
    Object value = claims.get(key);
    if (value == null) {
      return null;
    }
    String text = String.valueOf(value);
    return text.isBlank() ? null : text;
  }

  private boolean constantTimeEquals(String left, String right) {
    if (left == null || right == null) {
      return false;
    }
    if (left.length() != right.length()) {
      return false;
    }
    int result = 0;
    for (int i = 0; i < left.length(); i++) {
      result |= left.charAt(i) ^ right.charAt(i);
    }
    return result == 0;
  }
}
