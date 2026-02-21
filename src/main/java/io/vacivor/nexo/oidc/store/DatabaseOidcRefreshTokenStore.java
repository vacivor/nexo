package io.vacivor.nexo.oidc.store;

import io.micronaut.context.annotation.Primary;
import io.vacivor.nexo.core.RefreshTokenConsumeResult;
import io.vacivor.nexo.core.RefreshTokenConsumeStatus;
import io.vacivor.nexo.dal.entity.OidcRefreshTokenEntity;
import io.vacivor.nexo.dal.repository.OidcRefreshTokenRepository;
import io.vacivor.nexo.oidc.OidcRefreshToken;
import jakarta.inject.Singleton;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

@Singleton
@Primary
public class DatabaseOidcRefreshTokenStore implements OidcRefreshTokenStore {

  private final OidcRefreshTokenRepository repository;

  public DatabaseOidcRefreshTokenStore(OidcRefreshTokenRepository repository) {
    this.repository = repository;
  }

  @Override
  public void store(OidcRefreshToken token) {
    OidcRefreshTokenEntity entity = repository.findByToken(token.getToken()).orElseGet(OidcRefreshTokenEntity::new);
    entity.setToken(token.getToken());
    entity.setSubject(token.getSubject());
    entity.setClientId(token.getClientId());
    entity.setScopes(joinScopes(token.getScopes()));
    entity.setExpiresAt(token.getExpiresAt());
    entity.setFamilyId(token.getFamilyId());
    entity.setConsumed(false);
    entity.setRevoked(false);
    repository.save(entity);
  }

  @Override
  public Optional<OidcRefreshToken> find(String token) {
    return repository.findByToken(token)
        .filter(entity -> !entity.isRevoked() && !entity.isConsumed())
        .map(this::toToken);
  }

  @Override
  public Optional<OidcRefreshToken> consume(String token) {
    return consumeWithStatus(token).getToken();
  }

  @Override
  public RefreshTokenConsumeResult<OidcRefreshToken> consumeWithStatus(String token) {
    Optional<OidcRefreshTokenEntity> entityOpt = repository.findByToken(token);
    if (entityOpt.isEmpty()) {
      return RefreshTokenConsumeResult.status(RefreshTokenConsumeStatus.NOT_FOUND);
    }
    OidcRefreshTokenEntity entity = entityOpt.get();
    if (entity.isRevoked()) {
      return RefreshTokenConsumeResult.status(RefreshTokenConsumeStatus.REVOKED);
    }
    if (entity.isConsumed()) {
      revokeFamily(entity.getFamilyId());
      return RefreshTokenConsumeResult.status(RefreshTokenConsumeStatus.REUSED);
    }
    entity.setConsumed(true);
    repository.update(entity);
    return RefreshTokenConsumeResult.consumed(toToken(entity));
  }

  @Override
  public void revoke(String token) {
    repository.findByToken(token).ifPresent(entity -> {
      entity.setRevoked(true);
      repository.update(entity);
      if (entity.getFamilyId() != null && !entity.getFamilyId().isBlank()) {
        revokeFamily(entity.getFamilyId());
      }
    });
  }

  @Override
  public void revokeFamily(String familyId) {
    if (familyId == null || familyId.isBlank()) {
      return;
    }
    for (OidcRefreshTokenEntity entity : repository.findByFamilyId(familyId.trim())) {
      if (!entity.isRevoked()) {
        entity.setRevoked(true);
        repository.update(entity);
      }
    }
  }

  private OidcRefreshToken toToken(OidcRefreshTokenEntity entity) {
    return new OidcRefreshToken(
        entity.getToken(),
        entity.getSubject(),
        entity.getClientId(),
        parseScopes(entity.getScopes()),
        entity.getExpiresAt(),
        entity.getFamilyId());
  }

  private String joinScopes(Set<String> scopes) {
    if (scopes == null || scopes.isEmpty()) {
      return "";
    }
    return String.join(" ", scopes);
  }

  private Set<String> parseScopes(String value) {
    if (value == null || value.isBlank()) {
      return Set.of();
    }
    Set<String> scopes = new TreeSet<>();
    for (String scope : value.trim().split("\\s+")) {
      if (!scope.isBlank()) {
        scopes.add(scope);
      }
    }
    return scopes;
  }
}
