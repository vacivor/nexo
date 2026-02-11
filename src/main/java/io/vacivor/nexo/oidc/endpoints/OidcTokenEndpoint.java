package io.vacivor.nexo.oidc.endpoints;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

@Controller
public class OidcTokenEndpoint {

  @Post("/oidc/token")
  public HttpResponse<?> token() {
    return HttpResponse.ok();
  }

}
