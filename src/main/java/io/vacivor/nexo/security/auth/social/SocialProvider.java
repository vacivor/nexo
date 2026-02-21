package io.vacivor.nexo.security.auth.social;

import java.util.Arrays;
import java.util.Optional;

public enum SocialProvider {
  QQ("qq"),
  DINGTALK("dingtalk"),
  WECHAT("wechat"),
  GITHUB("github");

  private final String value;

  SocialProvider(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }

  public static Optional<SocialProvider> from(String provider) {
    if (provider == null) {
      return Optional.empty();
    }
    String normalized = provider.trim().toLowerCase();
    return Arrays.stream(values())
        .filter(v -> v.value.equals(normalized))
        .findFirst();
  }
}
