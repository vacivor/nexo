package io.vacivor.nexo.authorizationserver.client;

import io.micronaut.http.HttpRequest;
import jakarta.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Singleton
public class ClientCredentialsResolver {

  public Optional<ClientCredentials> resolve(HttpRequest<?> request, Map<String, String> body) {
    String clientId = trimToNull(body.get("client_id"));
    String clientSecret = trimToNull(body.get("client_secret"));
    if (clientId != null && clientSecret != null) {
      return Optional.of(new ClientCredentials(clientId, clientSecret));
    }
    Optional<ClientCredentials> basicCredentials = resolveBasicAuthorization(request);
    if (basicCredentials.isPresent()) {
      return basicCredentials;
    }
    if (clientId == null) {
      return Optional.empty();
    }
    return Optional.of(new ClientCredentials(clientId, clientSecret));
  }

  private Optional<ClientCredentials> resolveBasicAuthorization(HttpRequest<?> request) {
    String authorization = request.getHeaders().get("Authorization");
    if (authorization == null || !authorization.startsWith("Basic ")) {
      return Optional.empty();
    }
    try {
      String base64Part = authorization.substring("Basic ".length()).trim();
      String decoded = new String(Base64.getDecoder().decode(base64Part), StandardCharsets.UTF_8);
      int separator = decoded.indexOf(':');
      if (separator <= 0) {
        return Optional.empty();
      }
      String clientId = trimToNull(decoded.substring(0, separator));
      String clientSecret = trimToNull(decoded.substring(separator + 1));
      if (clientId == null) {
        return Optional.empty();
      }
      return Optional.of(new ClientCredentials(clientId, clientSecret));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  public record ClientCredentials(String clientId, String clientSecret) {
  }
}
