package io.vacivor.nexo.security.oidc.store;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.vacivor.nexo.security.oidc.OidcAuthorizationCode;
import io.vacivor.nexo.security.oidc.OidcConfiguration;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class InMemoryAuthorizationCodeStore implements OidcAuthorizationCodeStore {

  private final Cache<String, OidcAuthorizationCode> cache;

  public InMemoryAuthorizationCodeStore(OidcConfiguration configuration) {
    this.cache = Caffeine.newBuilder()
        .expireAfterWrite(configuration.getCodeTtl())
        .build();
  }

  @Override
  public void store(OidcAuthorizationCode code) {
    cache.put(code.getCode(), code);
  }

  @Override
  public Optional<OidcAuthorizationCode> consume(String code) {
    OidcAuthorizationCode stored = cache.getIfPresent(code);
    if (stored != null) {
      cache.invalidate(code);
    }
    return Optional.ofNullable(stored);
  }
}
