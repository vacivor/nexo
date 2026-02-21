package io.vacivor.nexo.admin.session;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.QueryValue;

@Controller("/api/admin/sessions")
public class AdminSessionController {

  private final AdminSessionService adminSessionService;

  public AdminSessionController(AdminSessionService adminSessionService) {
    this.adminSessionService = adminSessionService;
  }

  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<AdminSessionService.SessionPage> list(
      @QueryValue(defaultValue = "") String cursor,
      @QueryValue(defaultValue = "10") int limit) {
    int normalizedLimit = Math.max(1, Math.min(200, limit));
    return HttpResponse.ok(adminSessionService.listSessionsByCursor(cursor, normalizedLimit));
  }

  @Delete("/{id}")
  public HttpResponse<?> delete(@PathVariable String id) {
    return adminSessionService.deleteSession(id) ? HttpResponse.noContent() : HttpResponse.notFound();
  }
}
