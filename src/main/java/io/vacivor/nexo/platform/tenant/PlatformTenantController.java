package io.vacivor.nexo.platform.tenant;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.vacivor.nexo.admin.tenant.AdminTenantController;
import java.util.List;

@Controller("/api/platform/tenants")
public class PlatformTenantController {

  private final AdminTenantController delegate;

  public PlatformTenantController(AdminTenantController delegate) {
    this.delegate = delegate;
  }

  @Post
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> createTenant(@Body AdminTenantController.CreateTenantRequest request) {
    return delegate.createTenant(request);
  }

  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<List<AdminTenantController.TenantResponse>> listTenants() {
    return delegate.listTenants();
  }

  @Get("/{uuid}")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> getTenantByUuid(@PathVariable String uuid) {
    return delegate.getTenantByUuid(uuid);
  }
}
