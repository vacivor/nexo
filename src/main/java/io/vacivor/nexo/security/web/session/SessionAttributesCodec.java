package io.vacivor.nexo.security.web.session;

import io.micronaut.core.type.Argument;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.Map;

@Singleton
public class SessionAttributesCodec {

  private final ObjectMapper objectMapper;

  public SessionAttributesCodec(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public String encode(Map<String, Object> attributes) {
    try {
      return objectMapper.writeValueAsString(attributes);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to serialize session attributes", e);
    }
  }

  public Map<String, Object> decode(String json) {
    if (json == null || json.isBlank()) {
      return Map.of();
    }
    try {
      return objectMapper.readValue(json, Argument.mapOf(String.class, Object.class));
    } catch (IOException e) {
      throw new IllegalStateException("Failed to deserialize session attributes", e);
    }
  }
}
