package io.vacivor.nexo.authorizationserver.authorization;

import io.vacivor.nexo.dal.entity.ApplicationEntity;
import io.vacivor.nexo.dal.entity.UserEntity;
import io.vacivor.nexo.dal.repository.TenantUserRepository;
import io.vacivor.nexo.dal.repository.UserRepository;
import io.vacivor.nexo.oidc.OidcAuthorizationCode;
import io.vacivor.nexo.oidc.OidcClientService;
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
  private final OidcClientService clientService;
  private final UserRepository userRepository;
  private final TenantUserRepository tenantUserRepository;
  private final SecureRandom secureRandom = new SecureRandom();

  public AuthorizationService(OidcConfiguration configuration,
      OidcAuthorizationCodeStore codeStore,
      OidcClientService clientService,
      UserRepository userRepository,
      TenantUserRepository tenantUserRepository) {
    this.configuration = configuration;
    this.codeStore = codeStore;
    this.clientService = clientService;
    this.userRepository = userRepository;
    this.tenantUserRepository = tenantUserRepository;
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

  public boolean isUserTenantAllowedForClient(String username, String clientId) {
    if (username == null || username.isBlank() || clientId == null || clientId.isBlank()) {
      return false;
    }
    Optional<UserEntity> user = userRepository.findByUsername(username);
    Optional<ApplicationEntity> client = clientService.findEntityByClientId(clientId);
    if (user.isEmpty() || client.isEmpty()) {
      return false;
    }
    if (Boolean.TRUE.equals(user.get().getIsDeleted())) {
      return false;
    }
    String clientTenantId = normalize(client.get().getTenantId());
    if (clientTenantId == null) {
      return true;
    }
    return tenantUserRepository.existsActiveMembership(user.get().getId(), clientTenantId);
  }

  private String randomToken() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
