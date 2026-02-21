package io.vacivor.nexo.security.csrf;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.serde.annotation.Serdeable;

@Controller
public class CsrfController {

  private final CsrfService csrfService;
  private final CsrfConfiguration csrfConfiguration;

  public CsrfController(CsrfService csrfService, CsrfConfiguration csrfConfiguration) {
    this.csrfService = csrfService;
    this.csrfConfiguration = csrfConfiguration;
  }

  @Get("/csrf")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<CsrfTokenResponse> csrf() {
    if (!csrfConfiguration.isEnabled()) {
      return HttpResponse.ok(new CsrfTokenResponse(""));
    }
    MutableHttpResponse<CsrfTokenResponse> response = HttpResponse.ok();
    String token = csrfService.issueToken(response);
    response.body(new CsrfTokenResponse(token));
    return response;
  }

  @Serdeable
  private record CsrfTokenResponse(String token) {
  }
}
