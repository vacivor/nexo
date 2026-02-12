package io.vacivor.nexo.admin.application;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.serde.annotation.Serdeable;
import io.vacivor.nexo.ApplicationEntity;
import io.vacivor.nexo.ApplicationService;
import java.util.List;
import java.util.Map;

@Controller("/api/admin/applications")
public class AdminApplicationController {

  private final ApplicationService applicationService;

  public AdminApplicationController(ApplicationService applicationService) {
    this.applicationService = applicationService;
  }

  @Post
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> createApplication(@Body CreateApplicationRequest request) {
    if (request == null || request.tenantId() == null || request.tenantId().isBlank()) {
      return HttpResponse.badRequest(Map.of("error", "invalid_request"));
    }
    if (request.redirectUris() == null || request.redirectUris().isEmpty()) {
      return HttpResponse.badRequest(Map.of("error", "invalid_request"));
    }
    ApplicationEntity created = applicationService.createApplication(
        request.tenantId(),
        request.name(),
        request.redirectUris());
    return HttpResponse.ok(Map.of(
        "id", created.getId(),
        "uuid", created.getUuid(),
        "clientId", created.getClientId(),
        "clientSecret", created.getClientSecret(),
        "name", created.getName(),
        "redirectUris", applicationService.getRedirectUris(created)
    ));
  }

  @Introspected
  @Serdeable.Deserializable
  public record CreateApplicationRequest(String tenantId, String name, List<String> redirectUris) {

  }
}
