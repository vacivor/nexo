package io.vacivor.nexo;

import jakarta.inject.Singleton;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class ApplicationService {

  private final ApplicationRepository applicationRepository;
  private final SecureRandom secureRandom = new SecureRandom();

  public ApplicationService(ApplicationRepository applicationRepository) {
    this.applicationRepository = applicationRepository;
  }

  public ApplicationEntity createApplication(String tenantId, String name,
      List<String> redirectUris) {
    String clientId = "app_" + randomToken(12);
    String clientSecret = randomToken(32);
    String joined = String.join(",", redirectUris);
    ApplicationEntity applicationEntity = new ApplicationEntity();
    applicationEntity.setTenantId(tenantId);
    applicationEntity.setClientId(clientId);
    applicationEntity.setClientId(clientId);
    applicationEntity.setClientSecret(clientSecret);
    return applicationRepository.save(applicationEntity);
  }

  public Optional<ApplicationEntity> findByClientId(String clientId) {
    return applicationRepository.findByClientId(clientId);
  }

  public List<String> getRedirectUris(ApplicationEntity application) {
    return split(application.getRedirectUris());
  }

  public List<String> split(String redirectUris) {
    if (redirectUris == null || redirectUris.isBlank()) {
      return List.of();
    }
    return Arrays.stream(redirectUris.split(","))
        .map(String::trim)
        .filter(value -> !value.isBlank())
        .collect(Collectors.toList());
  }

  private String randomToken(int bytesLength) {
    byte[] bytes = new byte[bytesLength];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}

