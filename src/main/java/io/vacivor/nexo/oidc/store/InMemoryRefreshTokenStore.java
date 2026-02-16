package io.vacivor.nexo.oidc.store;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.vacivor.nexo.oidc.OidcConfiguration;
import io.vacivor.nexo.oidc.OidcRefreshToken;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class InMemoryRefreshTokenStore implements OidcRefreshTokenStore {

  private final Cache<String, OidcRefreshToken> cache;

  public InMemoryRefreshTokenStore(OidcConfiguration configuration) {
    this.cache = Caffeine.newBuilder()
        .expireAfterWrite(configuration.getRefreshTokenTtl())
        .build();
  }

  @Override
  public void store(OidcRefreshToken token) {
    cache.put(token.getToken(), token);
  }

  @Override
  public Optional<OidcRefreshToken> consume(String token) {
    OidcRefreshToken existing = cache.getIfPresent(token);
    if (existing != null) {
      cache.invalidate(token);
    }
    return Optional.ofNullable(existing);
  }
}
