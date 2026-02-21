package io.vacivor.nexo.platform.session;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.QueryValue;
import io.vacivor.nexo.admin.session.AdminSessionController;
import io.vacivor.nexo.admin.session.AdminSessionService;

@Controller("/api/platform/sessions")
public class PlatformSessionController {

  private final AdminSessionController delegate;

  public PlatformSessionController(AdminSessionController delegate) {
    this.delegate = delegate;
  }

  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<AdminSessionService.SessionPage> list(
      @QueryValue(defaultValue = "") String cursor,
      @QueryValue(defaultValue = "10") int limit) {
    return delegate.list(cursor, limit);
  }

  @Delete("/{id}")
  public HttpResponse<?> delete(@PathVariable String id) {
    return delegate.delete(id);
  }
}
