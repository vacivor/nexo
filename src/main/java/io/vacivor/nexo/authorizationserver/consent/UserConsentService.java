package io.vacivor.nexo.authorizationserver.consent;

import io.vacivor.nexo.dal.entity.ApplicationEntity;
import io.vacivor.nexo.dal.entity.OidcConsentEntity;
import io.vacivor.nexo.dal.repository.ApplicationRepository;
import io.vacivor.nexo.dal.repository.OidcConsentRepository;
import jakarta.inject.Singleton;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

@Singleton
public class UserConsentService {

  private final OidcConsentRepository consentRepository;
  private final ApplicationRepository applicationRepository;

  public UserConsentService(OidcConsentRepository consentRepository,
      ApplicationRepository applicationRepository) {
    this.consentRepository = consentRepository;
    this.applicationRepository = applicationRepository;
  }

  public List<ConsentGrantView> listBySubject(String subject) {
    if (subject == null || subject.isBlank()) {
      return List.of();
    }
    List<OidcConsentEntity> entities = consentRepository.findBySubjectOrderByUpdatedAtDesc(subject.trim());
    List<ConsentGrantView> result = new ArrayList<>(entities.size());
    for (OidcConsentEntity entity : entities) {
      ApplicationEntity application = applicationRepository.findByClientId(entity.getClientId()).orElse(null);
      String clientName = application != null && application.getName() != null && !application.getName().isBlank()
          ? application.getName()
          : entity.getClientId();
      String clientLogo = application == null ? null : application.getLogo();
      result.add(new ConsentGrantView(
          entity.getClientId(),
          clientName,
          clientLogo,
          parseScopes(entity.getScopes()),
          entity.getUpdatedAt()));
    }
    return result;
  }

  public boolean revoke(String subject, String clientId) {
    if (subject == null || subject.isBlank() || clientId == null || clientId.isBlank()) {
      return false;
    }
    return consentRepository.deleteBySubjectAndClientId(subject.trim(), clientId.trim()) > 0;
  }

  public RevokeScopesResult revokeScopes(String subject, String clientId, Set<String> scopesToRevoke) {
    if (subject == null || subject.isBlank() || clientId == null || clientId.isBlank()) {
      return RevokeScopesResult.notFound();
    }
    if (scopesToRevoke == null || scopesToRevoke.isEmpty()) {
      return RevokeScopesResult.invalidRequest();
    }
    Optional<OidcConsentEntity> entityOpt = consentRepository.findBySubjectAndClientId(subject.trim(), clientId.trim());
    if (entityOpt.isEmpty()) {
      return RevokeScopesResult.notFound();
    }
    OidcConsentEntity entity = entityOpt.get();
    Set<String> currentScopes = parseScopes(entity.getScopes());
    Set<String> normalizedRequested = normalizeScopes(scopesToRevoke);
    if (normalizedRequested.isEmpty()) {
      return RevokeScopesResult.invalidRequest();
    }
    boolean changed = currentScopes.removeAll(normalizedRequested);
    if (!changed) {
      return RevokeScopesResult.notFound();
    }
    if (currentScopes.isEmpty()) {
      consentRepository.delete(entity);
      return RevokeScopesResult.deleted();
    }
    entity.setScopes(String.join(" ", currentScopes));
    OidcConsentEntity updated = consentRepository.update(entity);
    return RevokeScopesResult.updated(parseScopes(updated.getScopes()));
  }

  private Set<String> parseScopes(String scopesValue) {
    if (scopesValue == null || scopesValue.isBlank()) {
      return Set.of();
    }
    Set<String> scopes = new TreeSet<>();
    scopes.addAll(Arrays.asList(scopesValue.trim().split("\\s+")));
    scopes.removeIf(String::isBlank);
    return scopes;
  }

  private Set<String> normalizeScopes(Set<String> rawScopes) {
    Set<String> normalized = new TreeSet<>();
    for (String scope : rawScopes) {
      if (scope == null) {
        continue;
      }
      String value = scope.trim();
      if (!value.isEmpty()) {
        normalized.add(value);
      }
    }
    return normalized;
  }

  public record ConsentGrantView(String clientId, String clientName, String clientLogo, Set<String> scopes,
                                 LocalDateTime grantedAt) {
  }

  public record RevokeScopesResult(RevokeScopesStatus status, Set<String> remainingScopes) {

    public static RevokeScopesResult notFound() {
      return new RevokeScopesResult(RevokeScopesStatus.NOT_FOUND, Set.of());
    }

    public static RevokeScopesResult invalidRequest() {
      return new RevokeScopesResult(RevokeScopesStatus.INVALID_REQUEST, Set.of());
    }

    public static RevokeScopesResult deleted() {
      return new RevokeScopesResult(RevokeScopesStatus.DELETED, Set.of());
    }

    public static RevokeScopesResult updated(Set<String> remainingScopes) {
      return new RevokeScopesResult(RevokeScopesStatus.UPDATED, remainingScopes);
    }
  }

  public enum RevokeScopesStatus {
    NOT_FOUND,
    INVALID_REQUEST,
    UPDATED,
    DELETED
  }
}
