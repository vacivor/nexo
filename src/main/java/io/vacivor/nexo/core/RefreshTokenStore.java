package io.vacivor.nexo.core;

import java.util.Optional;

public interface RefreshTokenStore<T extends RefreshToken> {

  void store(T token);

  default Optional<T> find(String token) {
    return Optional.empty();
  }

  Optional<T> consume(String token);

  default RefreshTokenConsumeResult<T> consumeWithStatus(String token) {
    return consume(token)
        .map(RefreshTokenConsumeResult::consumed)
        .orElseGet(() -> RefreshTokenConsumeResult.status(RefreshTokenConsumeStatus.NOT_FOUND));
  }

  default void revoke(String token) {
    // no-op
  }

  default void revokeFamily(String familyId) {
    // no-op
  }
}
