package io.vacivor.nexo.security.csrf;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import java.util.Map;

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
  public HttpResponse<Map<String, String>> csrf() {
    if (!csrfConfiguration.isEnabled()) {
      return HttpResponse.ok(Map.of("token", ""));
    }
    MutableHttpResponse<Map<String, String>> response = HttpResponse.ok();
    String token = csrfService.issueToken(response);
    response.body(Map.of(
        "token", token,
        "headerName", csrfConfiguration.getHeaderName(),
        "parameterName", csrfConfiguration.getParameterName()));
    return response;
  }
}
