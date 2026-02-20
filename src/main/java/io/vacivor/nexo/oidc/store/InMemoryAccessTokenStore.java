package io.vacivor.nexo.oidc.store;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.vacivor.nexo.oidc.OidcAccessToken;
import io.vacivor.nexo.oidc.OidcConfiguration;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class InMemoryAccessTokenStore implements OidcAccessTokenStore {

  private final Cache<String, OidcAccessToken> cache;
  private final Map<String, Set<String>> familyTokenIndex = new ConcurrentHashMap<>();

  public InMemoryAccessTokenStore(OidcConfiguration configuration) {
    this.cache = Caffeine.newBuilder()
        .expireAfterWrite(configuration.getAccessTokenTtl())
        .build();
  }

  @Override
  public void store(OidcAccessToken token) {
    cache.put(token.getToken(), token);
    if (token.getFamilyId() != null && !token.getFamilyId().isBlank()) {
      familyTokenIndex
          .computeIfAbsent(token.getFamilyId(), key -> ConcurrentHashMap.newKeySet())
          .add(token.getToken());
    }
  }

  @Override
  public Optional<OidcAccessToken> find(String token) {
    return Optional.ofNullable(cache.getIfPresent(token));
  }

  @Override
  public void revoke(String token) {
    OidcAccessToken existing = cache.getIfPresent(token);
    cache.invalidate(token);
    if (existing != null && existing.getFamilyId() != null && !existing.getFamilyId().isBlank()) {
      Set<String> tokens = familyTokenIndex.get(existing.getFamilyId());
      if (tokens != null) {
        tokens.remove(token);
        if (tokens.isEmpty()) {
          familyTokenIndex.remove(existing.getFamilyId());
        }
      }
    }
  }

  @Override
  public void revokeByFamily(String familyId) {
    if (familyId == null || familyId.isBlank()) {
      return;
    }
    Set<String> tokens = familyTokenIndex.remove(familyId);
    if (tokens == null || tokens.isEmpty()) {
      return;
    }
    for (String token : tokens) {
      cache.invalidate(token);
    }
  }
}
