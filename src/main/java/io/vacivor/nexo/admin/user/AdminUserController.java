package io.vacivor.nexo.admin.user;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
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
import io.micronaut.serde.annotation.Serdeable;
import io.vacivor.nexo.dal.entity.UserEntity;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller("/api/admin/users")
public class AdminUserController {

  private final AdminUserService adminUserService;

  public AdminUserController(AdminUserService adminUserService) {
    this.adminUserService = adminUserService;
  }

  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<List<AdminUserResponse>> list() {
    List<AdminUserResponse> users = adminUserService.listUsers().stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
    return HttpResponse.ok(users);
  }

  @Get("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> get(@PathVariable Long id) {
    return adminUserService.findUser(id)
        .<HttpResponse<?>>map(user -> HttpResponse.ok(toResponse(user)))
        .orElseGet(HttpResponse::notFound);
  }

  @Post
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> create(@Body AdminCreateUserRequest request) {
    if (request == null || isBlank(request.username()) || isBlank(request.password())) {
      return HttpResponse.badRequest(Map.of("error", "invalid_request"));
    }
    return adminUserService.createUser(
            request.username(), request.email(), request.phone(), request.password())
        .<HttpResponse<?>>map(user -> HttpResponse.ok(toResponse(user)))
        .orElseGet(() -> HttpResponse.status(HttpStatus.CONFLICT).body(Map.of("error", "user_exists")));
  }

  @Put("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> update(@PathVariable Long id, @Body AdminUpdateUserRequest request) {
    if (request == null || isBlank(request.username())) {
      return HttpResponse.badRequest(Map.of("error", "invalid_request"));
    }
    return adminUserService.updateUser(
            id, request.username(), request.email(), request.phone())
        .<HttpResponse<?>>map(user -> HttpResponse.ok(toResponse(user)))
        .orElseGet(() -> HttpResponse.status(HttpStatus.CONFLICT).body(Map.of("error", "user_exists_or_not_found")));
  }

  @Patch("/{id}/password")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> resetPassword(@PathVariable Long id, @Body AdminResetPasswordRequest request) {
    if (request == null || isBlank(request.password())) {
      return HttpResponse.badRequest(Map.of("error", "invalid_request"));
    }
    return adminUserService.resetPassword(id, request.password())
        .<HttpResponse<?>>map(user -> HttpResponse.ok(toResponse(user)))
        .orElseGet(HttpResponse::notFound);
  }

  @Delete("/{id}")
  public HttpResponse<?> delete(@PathVariable Long id) {
    return adminUserService.deleteUser(id) ? HttpResponse.noContent() : HttpResponse.notFound();
  }

  private AdminUserResponse toResponse(UserEntity user) {
    return new AdminUserResponse(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getPhone());
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  @Introspected
  @Serdeable.Deserializable
  public record AdminCreateUserRequest(String username, String email, String phone, String password) {
  }

  @Introspected
  @Serdeable.Deserializable
  public record AdminUpdateUserRequest(String username, String email, String phone) {
  }

  @Introspected
  @Serdeable.Deserializable
  public record AdminResetPasswordRequest(String password) {
  }

  @Introspected
  @Serdeable
  public record AdminUserResponse(Long id, String username, String email, String phone) {
  }
}
