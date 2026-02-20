package io.vacivor.nexo.oidc.endpoints;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.uri.UriBuilder;
import io.vacivor.nexo.authorizationserver.authorization.AuthorizationService;
import io.vacivor.nexo.oidc.OidcAuthorizationCode;
import io.vacivor.nexo.oidc.OidcConsentRequest;
import io.vacivor.nexo.oidc.OidcConsentService;
import io.micronaut.views.ModelAndView;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class OidcConsentEndpoint {

  private final OidcConsentService consentService;
  private final AuthorizationService authorizationService;

  public OidcConsentEndpoint(OidcConsentService consentService, AuthorizationService authorizationService) {
    this.consentService = consentService;
    this.authorizationService = authorizationService;
  }

  @Get(value = "/oidc/consent", produces = MediaType.TEXT_HTML)
  public HttpResponse<ModelAndView<Map<String, Object>>> consentPage(HttpRequest<?> request,
      @QueryValue("request_id") String requestId) {
    Optional<OidcConsentRequest> pending = consentService.findPendingRequest(request, requestId);
    if (pending.isEmpty()) {
      return HttpResponse.status(HttpStatus.BAD_REQUEST);
    }
    OidcConsentRequest consent = pending.get();
    Map<String, Object> model = new HashMap<>();
    model.put("requestId", consent.getRequestId());
    model.put("csrfToken", consent.getCsrfToken());
    model.put("clientId", consent.getClientId());
    model.put("scopes", consent.getScopes());
    return HttpResponse.ok(new ModelAndView<>("consent", model));
  }

  @Post(value = "/oidc/consent/approve", consumes = MediaType.APPLICATION_FORM_URLENCODED)
  public HttpResponse<?> approve(HttpRequest<?> request, @Body Map<String, String> body) {
    Optional<OidcConsentRequest> consent = validateCsrf(request, body);
    if (consent.isEmpty()) {
      return HttpResponse.status(HttpStatus.FORBIDDEN);
    }
    OidcConsentRequest approved = consent.get();
    consentService.approveConsent(approved.getSubject(), approved.getClientId(), approved.getScopes());
    OidcAuthorizationCode code = authorizationService.issueAuthorizationCode(approved.getClientId(),
        approved.getRedirectUri(), approved.getSubject(), approved.getScopes(), approved.getNonce());
    consentService.clearPendingRequest(request, approved.getRequestId());
    URI redirect = UriBuilder.of(approved.getRedirectUri())
        .queryParam("code", code.getCode())
        .queryParam("state", approved.getState())
        .build();
    return HttpResponse.redirect(redirect);
  }

  @Post(value = "/oidc/consent/deny", consumes = MediaType.APPLICATION_FORM_URLENCODED)
  public HttpResponse<?> deny(HttpRequest<?> request, @Body Map<String, String> body) {
    Optional<OidcConsentRequest> consent = validateCsrf(request, body);
    if (consent.isEmpty()) {
      return HttpResponse.status(HttpStatus.FORBIDDEN);
    }
    OidcConsentRequest denied = consent.get();
    consentService.clearPendingRequest(request, denied.getRequestId());
    URI redirect = UriBuilder.of(denied.getRedirectUri())
        .queryParam("error", "access_denied")
        .queryParam("state", denied.getState())
        .build();
    return HttpResponse.redirect(redirect);
  }

  private Optional<OidcConsentRequest> validateCsrf(HttpRequest<?> request, Map<String, String> body) {
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

}
