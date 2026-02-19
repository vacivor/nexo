package io.vacivor.nexo.admin.tenant;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Put;
import io.micronaut.serde.annotation.Serdeable;
import io.vacivor.nexo.dal.entity.TenantEntity;
import io.vacivor.nexo.TenantService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller("/api/admin/tenants")
public class AdminTenantController {

  private final TenantService tenantService;

  public AdminTenantController(TenantService tenantService) {
    this.tenantService = tenantService;
  }

  @Post
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> createTenant(@Body CreateTenantRequest request) {
    if (request == null || request.name() == null || request.name().isBlank()) {
      return HttpResponse.badRequest(Map.of("error", "invalid_request"));
    }
    TenantEntity tenant = tenantService.createTenant(request.name().trim());
    return HttpResponse.ok(toResponse(tenant));
  }

  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<List<TenantResponse>> listTenants() {
    List<TenantResponse> tenants = tenantService.findAll().stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
    return HttpResponse.ok(tenants);
  }

  @Get("/{uuid}")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> getTenantByUuid(@PathVariable String uuid) {
    return tenantService.findByUuid(uuid)
        .<HttpResponse<?>>map(tenant -> HttpResponse.ok(toResponse(tenant)))
        .orElseGet(HttpResponse::notFound);
  }

  @Put("/{uuid}")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> updateTenant(@PathVariable String uuid, @Body UpdateTenantRequest request) {
    if (request == null || request.name() == null || request.name().isBlank()) {
      return HttpResponse.badRequest(Map.of("error", "invalid_request"));
    }
    return tenantService.updateTenant(uuid, request.name().trim())
        .<HttpResponse<?>>map(tenant -> HttpResponse.ok(toResponse(tenant)))
        .orElseGet(HttpResponse::notFound);
  }

  private TenantResponse toResponse(TenantEntity entity) {
    return new TenantResponse(entity.getId(), entity.getUuid(), entity.getName());
  }

  @Introspected
  @Serdeable.Deserializable
  public record CreateTenantRequest(String name) {
  }

  @Introspected
  @Serdeable.Deserializable
  public record UpdateTenantRequest(String name) {
  }

  @Introspected
  @Serdeable
  public record TenantResponse(Long id, String uuid, String name) {
  }
}
