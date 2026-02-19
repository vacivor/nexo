package io.vacivor.nexo.tenant.provider;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Put;
import io.vacivor.nexo.admin.provider.AdminProviderController;
import java.util.List;

@Controller("/api/tenant/providers")
public class TenantProviderController {

  private final AdminProviderController delegate;

  public TenantProviderController(AdminProviderController delegate) {
    this.delegate = delegate;
  }

  @Post
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> create(@Body AdminProviderController.AdminProviderRequest request) {
    return delegate.create(request);
  }

  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<List<AdminProviderController.AdminProviderResponse>> list() {
    return delegate.list();
  }

  @Get("/{uuid}")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> get(@PathVariable String uuid) {
    return delegate.get(uuid);
  }

  @Put("/{uuid}")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> update(
      @PathVariable String uuid,
      @Body AdminProviderController.AdminProviderRequest request) {
    return delegate.update(uuid, request);
  }

  @Patch("/{uuid}/enabled/{enabled}")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> setEnabled(@PathVariable String uuid, @PathVariable boolean enabled) {
    return delegate.setEnabled(uuid, enabled);
  }

  @Delete("/{uuid}")
  public HttpResponse<?> delete(@PathVariable String uuid) {
    return delegate.delete(uuid);
  }
}
