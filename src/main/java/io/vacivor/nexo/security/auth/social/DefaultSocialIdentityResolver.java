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

import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;

@Singleton
@Requires(missingBeans = SocialIdentityResolver.class)
public class DefaultSocialIdentityResolver implements SocialIdentityResolver {

  @Override
  public Optional<SocialIdentity> resolve(SocialAuthenticationToken token) {
    if (token == null || token.getAccessToken() == null || token.getAccessToken().isBlank()) {
      return Optional.empty();
    }
    Optional<SocialProvider> provider = SocialProvider.from(token.getProvider());
    if (provider.isEmpty()) {
      return Optional.empty();
    }

    String providerUserId = sha256(token.getAccessToken().trim());
    SocialIdentity identity = new SocialIdentity();
    identity.setProvider(provider.get().value());
    identity.setProviderUserId(providerUserId);
    identity.setUsername(provider.get().value() + "_" + providerUserId.substring(0, 12));
    return Optional.of(identity);
  }

  private String sha256(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to hash social access token", e);
    }
  }
}