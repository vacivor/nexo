package io.vacivor.nexo.oidc.store;

import io.micronaut.context.annotation.Primary;
import io.vacivor.nexo.dal.entity.OidcAccessTokenEntity;
import io.vacivor.nexo.dal.repository.OidcAccessTokenRepository;
import io.vacivor.nexo.oidc.OidcAccessToken;
import jakarta.inject.Singleton;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

@Singleton
@Primary
public class DatabaseOidcAccessTokenStore implements OidcAccessTokenStore {

  private final OidcAccessTokenRepository repository;

  public DatabaseOidcAccessTokenStore(OidcAccessTokenRepository repository) {
    this.repository = repository;
  }

  @Override
  public void store(OidcAccessToken token) {
    OidcAccessTokenEntity entity = repository.findByToken(token.getToken()).orElseGet(OidcAccessTokenEntity::new);
    entity.setToken(token.getToken());
    entity.setSubject(token.getSubject());
    entity.setClientId(token.getClientId());
    entity.setScopes(joinScopes(token.getScopes()));
    entity.setExpiresAt(token.getExpiresAt());
    entity.setFamilyId(token.getFamilyId());
    entity.setRevoked(false);
    repository.save(entity);
  }

  @Override
  public Optional<OidcAccessToken> find(String token) {
    return repository.findByToken(token)
        .filter(entity -> !entity.isRevoked())
        .map(this::toToken);
  }

  @Override
  public void revoke(String token) {
    repository.findByToken(token).ifPresent(entity -> {
      entity.setRevoked(true);
      repository.update(entity);
    });
  }

  @Override
  public void revokeByFamily(String familyId) {
    if (familyId == null || familyId.isBlank()) {
      return;
    }
    for (OidcAccessTokenEntity entity : repository.findByFamilyId(familyId.trim())) {
      if (!entity.isRevoked()) {
        entity.setRevoked(true);
        repository.update(entity);
      }
    }
  }

  private OidcAccessToken toToken(OidcAccessTokenEntity entity) {
    return new OidcAccessToken(
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
