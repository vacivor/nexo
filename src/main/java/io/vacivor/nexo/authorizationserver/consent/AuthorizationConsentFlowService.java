package io.vacivor.nexo.authorizationserver.consent;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.views.ModelAndView;
import io.vacivor.nexo.oidc.OidcConsentRequest;
import io.vacivor.nexo.oidc.OidcConsentService;
import jakarta.inject.Singleton;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Singleton
public class AuthorizationConsentFlowService {

  private final OidcConsentService consentService;

  public AuthorizationConsentFlowService(OidcConsentService consentService) {
    this.consentService = consentService;
  }

  public Optional<HttpResponse<?>> resolveConsentRedirect(HttpRequest<?> request, String subject, String clientId,
      String redirectUri, Set<String> scopes, String state, String nonce, String consentPath) {
    if (consentService.hasConsent(subject, clientId, scopes)) {
      return Optional.empty();
    }
    Optional<OidcConsentRequest> pending = consentService.createPendingRequest(request, subject, clientId,
        redirectUri, scopes, state, nonce);
    if (pending.isEmpty()) {
      return Optional.of(HttpResponse.status(HttpStatus.UNAUTHORIZED));
    }
    URI consentPage = UriBuilder.of(consentPath)
        .queryParam("request_id", pending.get().getRequestId())
        .build();
    return Optional.of(HttpResponse.status(HttpStatus.FOUND).headers(headers -> headers.location(consentPage)));
  }

  public Optional<OidcConsentRequest> findPendingRequest(HttpRequest<?> request, String requestId) {
    return consentService.findPendingRequest(request, requestId);
  }

  public Optional<OidcConsentRequest> validateCsrf(HttpRequest<?> request, Map<String, String> body) {
    String requestId = body.get("request_id");
    String csrfToken = body.get("csrf_token");
    if (requestId == null || csrfToken == null) {
      return Optional.empty();
    }
    Optional<OidcConsentRequest> pending = consentService.findPendingRequest(request, requestId);
    if (pending.isEmpty()) {
      return Optional.empty();
    }
    if (!pending.get().getCsrfToken().equals(csrfToken)) {
      return Optional.empty();
    }
    return pending;
  }

  public ModelAndView<Map<String, Object>> buildConsentView(OidcConsentRequest consent, String approveAction,
      String denyAction) {
    Map<String, Object> model = new HashMap<>();
    model.put("requestId", consent.getRequestId());
    model.put("csrfToken", consent.getCsrfToken());
    model.put("clientId", consent.getClientId());
    model.put("scopes", consent.getScopes());
    model.put("approveAction", approveAction);
    model.put("denyAction", denyAction);
    return new ModelAndView<>("consent", model);
  }

  public URI approve(HttpRequest<?> request, OidcConsentRequest consent, String code) {
    consentService.approveConsent(consent.getSubject(), consent.getClientId(), consent.getScopes());
    consentService.clearPendingRequest(request, consent.getRequestId());
    return UriBuilder.of(consent.getRedirectUri())
        .queryParam("code", code)
        .queryParam("state", consent.getState())
        .build();
  }

  public URI deny(HttpRequest<?> request, OidcConsentRequest consent) {
    consentService.clearPendingRequest(request, consent.getRequestId());
    return UriBuilder.of(consent.getRedirectUri())
        .queryParam("error", "access_denied")
        .queryParam("state", consent.getState())
        .build();
  }
}
