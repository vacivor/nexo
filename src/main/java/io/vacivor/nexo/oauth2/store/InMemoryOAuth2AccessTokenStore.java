package io.vacivor.nexo.oauth2.store;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.vacivor.nexo.oidc.OidcConfiguration;
import io.vacivor.nexo.oauth2.token.OAuth2AccessToken;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class InMemoryOAuth2AccessTokenStore implements OAuth2AccessTokenStore {

  private final Cache<String, OAuth2AccessToken> cache;
  private final Map<String, Set<String>> familyTokenIndex = new ConcurrentHashMap<>();

  public InMemoryOAuth2AccessTokenStore(OidcConfiguration configuration) {
    this.cache = Caffeine.newBuilder()
        .expireAfterWrite(configuration.getAccessTokenTtl())
        .build();
  }

  @Override
  public void store(OAuth2AccessToken token) {
    cache.put(token.getToken(), token);
    if (token.getFamilyId() != null && !token.getFamilyId().isBlank()) {
      familyTokenIndex
          .computeIfAbsent(token.getFamilyId(), key -> ConcurrentHashMap.newKeySet())
          .add(token.getToken());
    }
  }

  @Override
  public Optional<OAuth2AccessToken> find(String token) {
    return Optional.ofNullable(cache.getIfPresent(token));
  }

  @Override
  public void revoke(String token) {
    OAuth2AccessToken existing = cache.getIfPresent(token);
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
