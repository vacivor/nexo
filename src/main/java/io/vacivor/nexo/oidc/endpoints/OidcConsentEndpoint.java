package io.vacivor.nexo.oidc.endpoints;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.vacivor.nexo.authorizationserver.authorization.AuthorizationService;
import io.vacivor.nexo.authorizationserver.consent.AuthorizationConsentFlowService;
import io.vacivor.nexo.oidc.OidcAuthorizationCode;
import io.vacivor.nexo.oidc.OidcConsentRequest;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Controller
public class OidcConsentEndpoint {

  private final AuthorizationConsentFlowService consentFlowService;
  private final AuthorizationService authorizationService;

  public OidcConsentEndpoint(AuthorizationConsentFlowService consentFlowService,
      AuthorizationService authorizationService) {
    this.consentFlowService = consentFlowService;
    this.authorizationService = authorizationService;
  }

  @Get("/oidc/consent")
  public HttpResponse<?> consentPage(HttpRequest<?> request,
      @QueryValue("request_id") String requestId) {
    Optional<OidcConsentRequest> pending = consentFlowService.findPendingRequest(request, requestId);
    if (pending.isEmpty()) {
      return HttpResponse.status(HttpStatus.BAD_REQUEST);
    }
    OidcConsentRequest consent = pending.get();
    return HttpResponse.ok(new ConsentViewResponse(
        consent.getRequestId(),
        consent.getCsrfToken(),
        consent.getClientId(),
        consent.getScopes()));
  }

  @Post("/oidc/consent/approve")
  public HttpResponse<?> approve(HttpRequest<?> request, @Body Map<String, String> body) {
    Optional<OidcConsentRequest> consent = consentFlowService.validateCsrf(request, body);
    if (consent.isEmpty()) {
      return HttpResponse.status(HttpStatus.FORBIDDEN);
    }
    OidcConsentRequest approved = consent.get();
    OidcAuthorizationCode code = authorizationService.issueAuthorizationCode(approved.getClientId(),
        approved.getRedirectUri(), approved.getSubject(), approved.getScopes(), approved.getNonce());
    String redirectUri = consentFlowService.approve(request, approved, code.getCode()).toString();
    return HttpResponse.ok(new ConsentDecisionResponse(redirectUri));
  }

  @Post("/oidc/consent/deny")
  public HttpResponse<?> deny(HttpRequest<?> request, @Body Map<String, String> body) {
    Optional<OidcConsentRequest> consent = consentFlowService.validateCsrf(request, body);
    if (consent.isEmpty()) {
      return HttpResponse.status(HttpStatus.FORBIDDEN);
    }
    String redirectUri = consentFlowService.deny(request, consent.get()).toString();
    return HttpResponse.ok(new ConsentDecisionResponse(redirectUri));
  }

  @Serdeable
  private record ConsentViewResponse(String requestId, String csrfToken, String clientId,
                                     Set<String> scopes) {
  }

  @Serdeable
  private record ConsentDecisionResponse(String redirectUri) {
  }
}
