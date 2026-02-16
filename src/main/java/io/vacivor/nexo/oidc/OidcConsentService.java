package io.vacivor.nexo.oidc;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.cookie.Cookie;
import io.vacivor.nexo.core.ConsentService;
import io.vacivor.nexo.dal.entity.OidcConsentEntity;
import io.vacivor.nexo.dal.repository.OidcConsentRepository;
import io.vacivor.nexo.security.web.session.Session;
import io.vacivor.nexo.security.web.session.SessionConfiguration;
import io.vacivor.nexo.security.web.session.SessionManager;
import jakarta.inject.Singleton;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Singleton
public class OidcConsentService implements ConsentService {

  private static final String CONSENT_PREFIX = "OIDC_CONSENT:";

  private final OidcConsentRepository consentRepository;
  private final SessionManager sessionManager;
  private final SessionConfiguration sessionConfiguration;
  private final SecureRandom random = new SecureRandom();

  public OidcConsentService(OidcConsentRepository consentRepository,
      SessionManager sessionManager,
      SessionConfiguration sessionConfiguration) {
    this.consentRepository = consentRepository;
    this.sessionManager = sessionManager;
    this.sessionConfiguration = sessionConfiguration;
  }

  public boolean hasConsent(String subject, String clientId, Set<String> requestedScopes) {
    return consentRepository.findBySubjectAndClientId(subject, clientId)
        .map(OidcConsentEntity::getScopes)
        .map(this::parseScopes)
        .map(existing -> existing.containsAll(requestedScopes))
        .orElse(false);
  }

  public void approveConsent(String subject, String clientId, Set<String> scopes) {
    OidcConsentEntity entity = consentRepository.findBySubjectAndClientId(subject, clientId)
        .orElseGet(OidcConsentEntity::new);
    entity.setSubject(subject);
    entity.setClientId(clientId);
    Set<String> merged = new TreeSet<>();
    if (entity.getScopes() != null && !entity.getScopes().isBlank()) {
      merged.addAll(parseScopes(entity.getScopes()));
    }
    merged.addAll(scopes);
    entity.setScopes(String.join(" ", merged));
    consentRepository.save(entity);
  }

  public Optional<OidcConsentRequest> createPendingRequest(HttpRequest<?> request, String subject,
      String clientId, String redirectUri, Set<String> scopes, String state, String nonce) {
    Optional<Session> sessionOpt = resolveSession(request);
    if (sessionOpt.isEmpty()) {
      return Optional.empty();
    }
    Session session = sessionOpt.get();
    String requestId = randomToken();
    String csrfToken = randomToken();
    OidcConsentRequest consentRequest = new OidcConsentRequest(requestId, csrfToken, subject, clientId,
        redirectUri, scopes, state, nonce);
    session.setAttribute(CONSENT_PREFIX + requestId, consentRequest);
    sessionManager.save(session);
    return Optional.of(consentRequest);
  }

  public Optional<OidcConsentRequest> findPendingRequest(HttpRequest<?> request, String requestId) {
    Optional<Session> session = resolveSession(request);
    if (session.isEmpty()) {
      return Optional.empty();
    }
    return session.get().getAttribute(CONSENT_PREFIX + requestId, OidcConsentRequest.class);
  }

  public void clearPendingRequest(HttpRequest<?> request, String requestId) {
    resolveSession(request).ifPresent(session -> {
      session.removeAttribute(CONSENT_PREFIX + requestId);
      sessionManager.save(session);
    });
  }

  private Optional<Session> resolveSession(HttpRequest<?> request) {
    String headerName = sessionConfiguration.getHeaderName();
    if (headerName != null && !headerName.isBlank()) {
      String headerValue = request.getHeaders().get(headerName);
      if (headerValue != null && !headerValue.isBlank()) {
        return sessionManager.findById(headerValue.trim());
      }
    }
    Cookie cookie = request.getCookies().get(sessionConfiguration.getCookieName());
    if (cookie == null || cookie.getValue() == null || cookie.getValue().isBlank()) {
      return Optional.empty();
    }
    return sessionManager.findById(cookie.getValue());
  }

  private Set<String> parseScopes(String scopes) {
    if (scopes == null || scopes.isBlank()) {
      return Collections.emptySet();
    }
    return Arrays.stream(scopes.trim().split("\\s+"))
        .filter(s -> !s.isBlank())
        .collect(Collectors.toCollection(TreeSet::new));
  }

  private String randomToken() {
    byte[] bytes = new byte[24];
    random.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
