package io.vacivor.nexo.admin.application;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Put;
import io.micronaut.serde.annotation.Serdeable;
import io.vacivor.nexo.dal.entity.ApplicationEntity;
import io.vacivor.nexo.admin.application.ApplicationService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller("/api/admin/applications")
public class AdminApplicationController {

  private final ApplicationService applicationService;

  public AdminApplicationController(ApplicationService applicationService) {
    this.applicationService = applicationService;
  }

  @Post
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> createApplication(@Body CreateApplicationRequest request) {
    if (request == null || isBlank(request.name()) || isBlank(request.clientType())) {
      return HttpResponse.badRequest(Map.of("error", "invalid_request"));
    }
    ApplicationEntity created = applicationService.createApplication(
        request.clientType().trim(),
        request.name().trim(),
        isBlank(request.description()) ? null : request.description().trim());
    return HttpResponse.ok(toResponse(created));
  }

  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<List<ApplicationResponse>> listApplications() {
    List<ApplicationResponse> result = applicationService.findAll().stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
    return HttpResponse.ok(result);
  }

  @Get("/{uuid}")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> getApplication(@PathVariable String uuid) {
    return applicationService.findByUuid(uuid)
        .<HttpResponse<?>>map(app -> HttpResponse.ok(toResponse(app)))
        .orElseGet(HttpResponse::notFound);
  }

  @Put("/{uuid}")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> updateApplication(@PathVariable String uuid,
      @Body UpdateApplicationRequest request) {
    if (request == null) {
      return HttpResponse.badRequest(Map.of("error", "invalid_request"));
    }
    List<String> redirectUris = applicationService.normalizeRedirectUris(request.redirectUris());
    return applicationService.updateApplication(
            uuid,
            isBlank(request.clientType()) ? null : request.clientType().trim(),
            isBlank(request.name()) ? null : request.name().trim(),
            isBlank(request.description()) ? null : request.description().trim(),
            isBlank(request.logo()) ? null : request.logo().trim(),
            positiveOrNull(request.idTokenExpiration()),
            positiveOrNull(request.refreshTokenExpiration()),
            redirectUris)
        .<HttpResponse<?>>map(app -> HttpResponse.ok(toResponse(app)))
        .orElseGet(HttpResponse::notFound);
  }

  @Delete("/{uuid}")
  public HttpResponse<?> deleteApplication(@PathVariable String uuid) {
    return applicationService.deleteByUuid(uuid)
        ? HttpResponse.noContent()
        : HttpResponse.status(HttpStatus.NOT_FOUND);
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private Integer positiveOrNull(Integer value) {
    if (value == null || value <= 0) {
      return null;
    }
    return value;
  }

  private ApplicationResponse toResponse(ApplicationEntity application) {
    return new ApplicationResponse(
        application.getId(),
        application.getUuid(),
        application.getClientId(),
        application.getClientSecret(),
        application.getName(),
        application.getDescription(),
        application.getLogo(),
        application.getClientType(),
        application.getIdTokenExpiration(),
        application.getRefreshTokenExpiration(),
        applicationService.getRedirectUris(application));
  }

  @Introspected
  @Serdeable.Deserializable
  public record CreateApplicationRequest(String clientType, String name, String description) {

  }

  @Introspected
  @Serdeable.Deserializable
  public record UpdateApplicationRequest(String clientType, String name, String description,
                                         String logo,
                                         Integer idTokenExpiration, Integer refreshTokenExpiration,
                                         List<String> redirectUris) {
  }

  @Introspected
  @Serdeable
  public record ApplicationResponse(Long id, String uuid, String clientId,
                                    String clientSecret, String name, String description,
                                    String logo, String clientType,
                                    Integer idTokenExpiration, Integer refreshTokenExpiration,
                                    List<String> redirectUris) {
  }
}
