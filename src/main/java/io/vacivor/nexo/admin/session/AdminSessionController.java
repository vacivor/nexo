package io.vacivor.nexo.admin.session;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.QueryValue;
import java.util.List;

@Controller("/api/admin/sessions")
public class AdminSessionController {

  private final AdminSessionService adminSessionService;

  public AdminSessionController(AdminSessionService adminSessionService) {
    this.adminSessionService = adminSessionService;
  }

  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<List<AdminSessionService.SessionView>> list(
      @QueryValue(defaultValue = "0") int offset,
      @QueryValue(defaultValue = "20") int limit,
      @QueryValue(defaultValue = "false") boolean includeTotal) {
    int normalizedOffset = Math.max(0, offset);
    int normalizedLimit = Math.max(1, Math.min(200, limit));
    MutableHttpResponse<List<AdminSessionService.SessionView>> response =
        HttpResponse.ok(adminSessionService.listSessions(normalizedOffset, normalizedLimit));
    if (includeTotal) {
      response.getHeaders().add("X-Total-Count", String.valueOf(adminSessionService.countSessions()));
    }
    return response;
  }

  @Delete("/{id}")
  public HttpResponse<?> delete(@PathVariable String id) {
    return adminSessionService.deleteSession(id) ? HttpResponse.noContent() : HttpResponse.notFound();
  }
}
