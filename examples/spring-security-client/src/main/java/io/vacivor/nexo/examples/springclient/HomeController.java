package io.vacivor.nexo.examples.springclient;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

  @GetMapping("/")
  public String index(Model model, Principal principal) {
    model.addAttribute("principalName", principal == null ? null : principal.getName());
    return "index";
  }

  @GetMapping("/public")
  @ResponseBody
  public Map<String, Object> publicEndpoint() {
    return Map.of("message", "public ok");
  }

  @GetMapping("/me")
  @ResponseBody
  public Map<String, Object> me(Authentication authentication) {
    if (authentication == null) {
      return Map.of();
    }
    if (authentication instanceof OAuth2AuthenticationToken oauth2Token
        && oauth2Token.getPrincipal() instanceof OidcUser oidcUser) {
      Map<String, Object> response = new LinkedHashMap<>();
      response.put("name", oidcUser.getName());
      response.put("email", oidcUser.getEmail());
      response.put("authorities", oauth2Token.getAuthorities());
      response.put("claims", oidcUser.getClaims());
      return response;
    }
    return Map.of(
        "name", authentication.getName(),
        "authorities", authentication.getAuthorities());
  }

  @GetMapping("/token")
  @ResponseBody
  public Map<String, Object> token(
      @RegisteredOAuth2AuthorizedClient("nexo") OAuth2AuthorizedClient authorizedClient,
      Authentication authentication) {
    Map<String, Object> response = new LinkedHashMap<>();
    if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
      return response;
    }
    response.put("principal", authentication == null ? null : authentication.getName());
    response.put("access_token", authorizedClient.getAccessToken().getTokenValue());
    response.put("token_type", authorizedClient.getAccessToken().getTokenType().getValue());
    response.put("expires_at", authorizedClient.getAccessToken().getExpiresAt());
    response.put("scopes", authorizedClient.getAccessToken().getScopes());
    return response;
  }
}
