package io.vacivor.nexo.core;

import java.util.Optional;

public class RefreshTokenConsumeResult<T extends RefreshToken> {

  private final RefreshTokenConsumeStatus status;
  private final T token;

  private RefreshTokenConsumeResult(RefreshTokenConsumeStatus status, T token) {
    this.status = status;
    this.token = token;
  }

  public static <T extends RefreshToken> RefreshTokenConsumeResult<T> consumed(T token) {
    return new RefreshTokenConsumeResult<>(RefreshTokenConsumeStatus.CONSUMED, token);
  }

  public static <T extends RefreshToken> RefreshTokenConsumeResult<T> status(RefreshTokenConsumeStatus status) {
    return new RefreshTokenConsumeResult<>(status, null);
  }

  public RefreshTokenConsumeStatus getStatus() {
    return status;
  }

  public Optional<T> getToken() {
    return Optional.ofNullable(token);
  }
}
