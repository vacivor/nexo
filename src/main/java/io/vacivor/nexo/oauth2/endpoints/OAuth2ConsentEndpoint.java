package io.vacivor.nexo.oauth2.endpoints;

import io.micronaut.context.annotation.Requires;
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

@Controller
@Requires(property = "nexo.oauth2.enabled", value = "true", defaultValue = "false")
public class OAuth2ConsentEndpoint {

  private final AuthorizationConsentFlowService consentFlowService;
  private final AuthorizationService authorizationService;

  public OAuth2ConsentEndpoint(AuthorizationConsentFlowService consentFlowService,
      AuthorizationService authorizationService) {
    this.consentFlowService = consentFlowService;
    this.authorizationService = authorizationService;
  }

  @Get("/oauth/consent")
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

  @Post("/oauth/consent/approve")
  public HttpResponse<?> approve(HttpRequest<?> request, @Body Map<String, String> body) {
    Optional<OidcConsentRequest> consent = consentFlowService.validateCsrf(request, body);
    if (consent.isEmpty()) {
      return HttpResponse.status(HttpStatus.FORBIDDEN);
    }
    OidcConsentRequest approved = consent.get();
    OidcAuthorizationCode code = authorizationService.issueAuthorizationCode(approved.getClientId(),
        approved.getRedirectUri(), approved.getSubject(), approved.getScopes(), null);
    String redirectUri = consentFlowService.approve(request, approved, code.getCode()).toString();
    return HttpResponse.ok(new ConsentDecisionResponse(redirectUri));
  }

  @Post("/oauth/consent/deny")
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
                                     java.util.Set<String> scopes) {
  }

  @Serdeable
  private record ConsentDecisionResponse(String redirectUri) {
  }
}
