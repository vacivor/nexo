package io.vacivor.nexo.platform.user;

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
import io.vacivor.nexo.admin.user.AdminUserController;
import java.util.List;

@Controller("/api/platform/users")
public class PlatformUserController {

  private final AdminUserController delegate;

  public PlatformUserController(AdminUserController delegate) {
    this.delegate = delegate;
  }

  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<List<AdminUserController.AdminUserResponse>> list() {
    return delegate.list();
  }

  @Get("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> get(@PathVariable Long id) {
    return delegate.get(id);
  }

  @Post
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> create(@Body AdminUserController.AdminCreateUserRequest request) {
    return delegate.create(request);
  }

  @Put("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> update(
      @PathVariable Long id,
      @Body AdminUserController.AdminUpdateUserRequest request) {
    return delegate.update(id, request);
  }

  @Patch("/{id}/password")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> resetPassword(
      @PathVariable Long id,
      @Body AdminUserController.AdminResetPasswordRequest request) {
    return delegate.resetPassword(id, request);
  }

  @Delete("/{id}")
  public HttpResponse<?> delete(@PathVariable Long id) {
    return delegate.delete(id);
  }
}
