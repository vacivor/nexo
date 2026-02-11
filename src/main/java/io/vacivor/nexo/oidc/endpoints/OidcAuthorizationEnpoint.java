package io.vacivor.nexo.oidc.endpoints;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller
public class OidcAuthorizationEnpoint {

  @Get("/oidc/authorize")
  public HttpResponse<?> authorize(String clientId, String responseType,
      String redirectUri, String scope, String state) {
    return HttpResponse.ok();
  }

}
