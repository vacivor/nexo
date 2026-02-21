package io.vacivor.nexo.authorizationserver.authorization;

import io.vacivor.nexo.oidc.OidcAuthorizationCode;
import io.vacivor.nexo.oidc.OidcConfiguration;
import io.vacivor.nexo.oidc.store.OidcAuthorizationCodeStore;
import jakarta.inject.Singleton;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;

@Singleton
public class AuthorizationService {

  private final OidcConfiguration configuration;
  private final OidcAuthorizationCodeStore codeStore;
  private final SecureRandom secureRandom = new SecureRandom();

  public AuthorizationService(OidcConfiguration configuration,
      OidcAuthorizationCodeStore codeStore) {
    this.configuration = configuration;
    this.codeStore = codeStore;
  }

  public OidcAuthorizationCode issueAuthorizationCode(String clientId, String redirectUri,
      String subject, Set<String> scopes, String nonce) {
    String code = randomToken();
    Instant expiresAt = Instant.now().plus(configuration.getCodeTtl());
    OidcAuthorizationCode authCode = new OidcAuthorizationCode(code, clientId, redirectUri, subject, scopes,
        nonce, expiresAt);
    codeStore.store(authCode);
    return authCode;
  }

  public Optional<OidcAuthorizationCode> consumeCode(String code) {
    return codeStore.consume(code).filter(c -> c.getExpiresAt().isAfter(Instant.now()));
  }

  private String randomToken() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

}
