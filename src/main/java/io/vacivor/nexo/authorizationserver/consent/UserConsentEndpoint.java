package io.vacivor.nexo.authorizationserver.consent;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Body;
import io.micronaut.serde.annotation.Serdeable;
import io.vacivor.nexo.security.auth.core.Authentication;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Controller("/api/account/consents")
public class UserConsentEndpoint {

  private final UserConsentService userConsentService;

  public UserConsentEndpoint(UserConsentService userConsentService) {
    this.userConsentService = userConsentService;
  }

  @Get
  public HttpResponse<List<ConsentGrantResponse>> list(@Nullable Authentication authentication) {
    if (authentication == null || authentication.getPrincipal() == null) {
      return HttpResponse.unauthorized();
    }
    String subject = String.valueOf(authentication.getPrincipal());
    List<UserConsentService.ConsentGrantView> grants = userConsentService.listBySubject(subject);
    List<ConsentGrantResponse> response = new ArrayList<>(grants.size());
    for (UserConsentService.ConsentGrantView grant : grants) {
      response.add(new ConsentGrantResponse(
          grant.clientId(),
          grant.clientName(),
          grant.clientLogo(),
          grant.scopes(),
          grant.grantedAt()));
    }
    return HttpResponse.ok(response);
  }

  @Delete("/{clientId}")
  public HttpResponse<?> revoke(@Nullable Authentication authentication, @PathVariable String clientId) {
    if (authentication == null || authentication.getPrincipal() == null) {
      return HttpResponse.unauthorized();
    }
    String subject = String.valueOf(authentication.getPrincipal());
    boolean deleted = userConsentService.revoke(subject, clientId);
    if (!deleted) {
      return HttpResponse.notFound();
    }
    return HttpResponse.noContent();
  }

  @Post("/{clientId}/revoke-scopes")
  public HttpResponse<?> revokeScopes(@Nullable Authentication authentication, @PathVariable String clientId,
      @Body Map<String, Object> request) {
    if (authentication == null || authentication.getPrincipal() == null) {
      return HttpResponse.unauthorized();
    }
    Set<String> scopes = parseScopes(request.get("scopes"));
    String subject = String.valueOf(authentication.getPrincipal());
    UserConsentService.RevokeScopesResult result = userConsentService.revokeScopes(subject, clientId,
        scopes);
    if (result.status() == UserConsentService.RevokeScopesStatus.INVALID_REQUEST) {
      return HttpResponse.badRequest();
    }
    if (result.status() == UserConsentService.RevokeScopesStatus.NOT_FOUND) {
      return HttpResponse.notFound();
    }
    if (result.status() == UserConsentService.RevokeScopesStatus.DELETED) {
      return HttpResponse.noContent();
    }
    return HttpResponse.ok(new RevokeScopesResponse(result.remainingScopes()));
  }

  @Serdeable
  private record ConsentGrantResponse(String clientId, String clientName, String clientLogo, Set<String> scopes,
                                      LocalDateTime grantedAt) {
  }

  @Serdeable
  private record RevokeScopesResponse(Set<String> remainingScopes) {
  }

  private Set<String> parseScopes(Object rawScopes) {
    if (!(rawScopes instanceof List<?> list)) {
      return Set.of();
    }
    Set<String> scopes = new TreeSet<>();
    for (Object value : list) {
      if (value == null) {
        continue;
      }
      String scope = String.valueOf(value).trim();
      if (!scope.isEmpty()) {
        scopes.add(scope);
      }
    }
    return scopes;
  }
}
