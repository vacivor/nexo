package io.vacivor.nexo.security.core.session;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

public final class SessionCursor {

  private SessionCursor() {
  }

  public static String encode(Session session) {
    String raw = session.getLastAccessedTime().toEpochMilli() + "|" + session.getId();
    return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  public static Optional<CursorValue> decode(String cursor) {
    if (cursor == null || cursor.isBlank()) {
      return Optional.empty();
    }
    try {
      byte[] decoded = Base64.getUrlDecoder().decode(cursor);
      String value = new String(decoded, StandardCharsets.UTF_8);
      int split = value.indexOf('|');
      if (split <= 0 || split >= value.length() - 1) {
        return Optional.empty();
      }
      long epochMillis = Long.parseLong(value.substring(0, split));
      String id = value.substring(split + 1);
      if (id.isBlank()) {
        return Optional.empty();
      }
      return Optional.of(new CursorValue(Instant.ofEpochMilli(epochMillis), id));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public static boolean isAfterCursor(Session session, CursorValue cursor) {
    if (session.getLastAccessedTime().isBefore(cursor.lastAccessedAt())) {
      return true;
    }
    if (session.getLastAccessedTime().equals(cursor.lastAccessedAt())) {
      return session.getId().compareTo(cursor.id()) < 0;
    }
    return false;
  }

  public record CursorValue(Instant lastAccessedAt, String id) {
  }
}
