package io.vacivor.nexo.oauth2.store;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.vacivor.nexo.core.RefreshTokenConsumeResult;
import io.vacivor.nexo.core.RefreshTokenConsumeStatus;
import io.vacivor.nexo.oidc.OidcConfiguration;
import io.vacivor.nexo.oauth2.token.OAuth2RefreshToken;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class InMemoryOAuth2RefreshTokenStore implements OAuth2RefreshTokenStore {

  private final Cache<String, OAuth2RefreshToken> cache;
  private final Cache<String, String> consumedTokenFamily;
  private final Cache<String, Boolean> revokedFamilies;
  private final Map<String, Set<String>> familyTokenIndex = new ConcurrentHashMap<>();

  public InMemoryOAuth2RefreshTokenStore(OidcConfiguration configuration) {
    this.cache = Caffeine.newBuilder()
        .expireAfterWrite(configuration.getRefreshTokenTtl())
        .build();
    this.consumedTokenFamily = Caffeine.newBuilder()
        .expireAfterWrite(configuration.getRefreshTokenTtl())
        .build();
    this.revokedFamilies = Caffeine.newBuilder()
        .expireAfterWrite(configuration.getRefreshTokenTtl())
        .build();
  }

  @Override
  public void store(OAuth2RefreshToken token) {
    cache.put(token.getToken(), token);
    if (token.getFamilyId() != null && !token.getFamilyId().isBlank()) {
      familyTokenIndex
          .computeIfAbsent(token.getFamilyId(), key -> ConcurrentHashMap.newKeySet())
          .add(token.getToken());
    }
  }

  @Override
  public Optional<OAuth2RefreshToken> find(String token) {
    return Optional.ofNullable(cache.getIfPresent(token));
  }

  @Override
  public Optional<OAuth2RefreshToken> consume(String token) {
    return consumeWithStatus(token).getToken();
  }

  @Override
  public RefreshTokenConsumeResult<OAuth2RefreshToken> consumeWithStatus(String token) {
    OAuth2RefreshToken existing = cache.getIfPresent(token);
    if (existing == null) {
      String reusedFamily = consumedTokenFamily.getIfPresent(token);
      if (reusedFamily != null && !reusedFamily.isBlank()) {
        revokeFamily(reusedFamily);
        return RefreshTokenConsumeResult.status(RefreshTokenConsumeStatus.REUSED);
      }
      return RefreshTokenConsumeResult.status(RefreshTokenConsumeStatus.NOT_FOUND);
    }
    String familyId = existing.getFamilyId();
    if (familyId != null && !familyId.isBlank()) {
      if (Boolean.TRUE.equals(revokedFamilies.getIfPresent(familyId))) {
        cache.invalidate(token);
        removeFromFamilyIndex(familyId, token);
        return RefreshTokenConsumeResult.status(RefreshTokenConsumeStatus.REVOKED);
      }
      consumedTokenFamily.put(token, familyId);
    }
    cache.invalidate(token);
    removeFromFamilyIndex(familyId, token);
    return RefreshTokenConsumeResult.consumed(existing);
  }

  @Override
  public void revoke(String token) {
    OAuth2RefreshToken existing = cache.getIfPresent(token);
    cache.invalidate(token);
    if (existing == null) {
      return;
    }
    String familyId = existing.getFamilyId();
    removeFromFamilyIndex(familyId, token);
    if (familyId != null && !familyId.isBlank()) {
      consumedTokenFamily.put(token, familyId);
      revokedFamilies.put(familyId, true);
    }
  }

  @Override
  public void revokeFamily(String familyId) {
    if (familyId == null || familyId.isBlank()) {
      return;
    }
    revokedFamilies.put(familyId, true);
    Set<String> activeTokens = familyTokenIndex.remove(familyId);
    if (activeTokens == null || activeTokens.isEmpty()) {
      return;
    }
    for (String token : activeTokens) {
      cache.invalidate(token);
      consumedTokenFamily.put(token, familyId);
    }
  }

  private void removeFromFamilyIndex(String familyId, String token) {
    if (familyId == null || familyId.isBlank()) {
      return;
    }
    Set<String> tokens = familyTokenIndex.get(familyId);
    if (tokens == null) {
      return;
    }
    tokens.remove(token);
    if (tokens.isEmpty()) {
      familyTokenIndex.remove(familyId);
    }
  }
}
