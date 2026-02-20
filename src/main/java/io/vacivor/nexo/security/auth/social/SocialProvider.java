package io.vacivor.nexo.security.auth.social;

import io.vacivor.nexo.security.auth.core.*;
import io.vacivor.nexo.security.auth.service.*;
import io.vacivor.nexo.security.auth.persistence.*;
import io.vacivor.nexo.security.auth.handler.*;
import io.vacivor.nexo.security.auth.password.*;
import io.vacivor.nexo.security.auth.provider.local.*;
import io.vacivor.nexo.security.auth.client.*;
import io.vacivor.nexo.security.auth.dto.*;
import io.vacivor.nexo.security.auth.web.*;

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