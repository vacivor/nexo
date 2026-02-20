package io.vacivor.nexo.examples.springclient;

import java.security.Principal;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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
      return Map.of(
          "name", oidcUser.getName(),
          "email", oidcUser.getEmail(),
          "authorities", oauth2Token.getAuthorities(),
          "claims", oidcUser.getClaims());
    }
    return Map.of(
        "name", authentication.getName(),
        "authorities", authentication.getAuthorities());
  }
}
