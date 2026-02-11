package io.vacivor.nexo;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller("/api/applications")
public class ApplicationController {

  private final ApplicationService applicationService;

  public ApplicationController(ApplicationService applicationService) {
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
    return HttpResponse.ok();
  }

  @Introspected
  @Serdeable.Deserializable
  public record CreateApplicationRequest(String tenantId, String name, List<String> redirectUris) {

  }
}

