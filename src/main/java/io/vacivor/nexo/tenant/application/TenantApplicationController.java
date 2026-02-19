package io.vacivor.nexo.tenant.application;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Put;
import io.vacivor.nexo.admin.application.AdminApplicationController;
import java.util.List;

@Controller("/api/tenant/applications")
public class TenantApplicationController {

  private final AdminApplicationController delegate;

  public TenantApplicationController(AdminApplicationController delegate) {
    this.delegate = delegate;
  }

  @Post
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> createApplication(
      @Body AdminApplicationController.CreateApplicationRequest request) {
    return delegate.createApplication(request);
  }

  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<List<AdminApplicationController.ApplicationResponse>> listApplications() {
    return delegate.listApplications();
  }

  @Get("/{uuid}")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> getApplication(@PathVariable String uuid) {
    return delegate.getApplication(uuid);
  }

  @Put("/{uuid}")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> updateApplication(
      @PathVariable String uuid,
      @Body AdminApplicationController.UpdateApplicationRequest request) {
    return delegate.updateApplication(uuid, request);
  }

  @Delete("/{uuid}")
  public HttpResponse<?> deleteApplication(@PathVariable String uuid) {
    return delegate.deleteApplication(uuid);
  }
}
