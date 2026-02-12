package io.vacivor.nexo.security.oidc.store;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.vacivor.nexo.security.oidc.OidcAccessToken;
import io.vacivor.nexo.security.oidc.OidcConfiguration;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class InMemoryAccessTokenStore implements OidcAccessTokenStore {

  private final Cache<String, OidcAccessToken> cache;

  public InMemoryAccessTokenStore(OidcConfiguration configuration) {
    this.cache = Caffeine.newBuilder()
        .expireAfterWrite(configuration.getAccessTokenTtl())
        .build();
  }

  @Override
  public void store(OidcAccessToken token) {
    cache.put(token.getToken(), token);
  }

  @Override
  public Optional<OidcAccessToken> find(String token) {
    return Optional.ofNullable(cache.getIfPresent(token));
  }
}
