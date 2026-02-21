package io.vacivor.nexo.admin.application;

import io.vacivor.nexo.client.ClientIdGenerator;
import io.vacivor.nexo.dal.entity.ApplicationEntity;
import io.vacivor.nexo.dal.repository.ApplicationRepository;
import jakarta.inject.Singleton;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class ApplicationService {

  private static final int MAX_CLIENT_ID_GENERATION_ATTEMPTS = 8;
  private final ApplicationRepository applicationRepository;
  private final ClientIdGenerator clientIdGenerator;
  private final SecureRandom secureRandom = new SecureRandom();

  public ApplicationService(
      ApplicationRepository applicationRepository,
      ClientIdGenerator clientIdGenerator) {
    this.applicationRepository = applicationRepository;
    this.clientIdGenerator = clientIdGenerator;
  }

  public ApplicationEntity createApplication(String clientType, String name, String description) {
    String clientId = generateUniqueClientId();
    String clientSecret = randomToken(32);
    ApplicationEntity applicationEntity = new ApplicationEntity();
    applicationEntity.setUuid(UUID.randomUUID().toString());
    applicationEntity.setName(name);
    applicationEntity.setDescription(description);
    applicationEntity.setClientType(clientType);
    applicationEntity.setClientId(clientId);
    applicationEntity.setClientSecret(clientSecret);
    applicationEntity.setRedirectUris("");
    applicationEntity.setIdTokenExpiration(null);
    applicationEntity.setRefreshTokenExpiration(null);
    return applicationRepository.save(applicationEntity);
  }

  public Optional<ApplicationEntity> findByClientId(String clientId) {
    return applicationRepository.findByClientId(clientId);
  }

  public Optional<ApplicationEntity> findByUuid(String uuid) {
    return applicationRepository.findByUuid(uuid);
  }

  public List<ApplicationEntity> findAll() {
    return applicationRepository.findAll();
  }

  public Optional<ApplicationEntity> updateApplication(
      String uuid,
      String clientType,
      String name,
      String description,
      String logo,
      Integer idTokenExpiration,
      Integer refreshTokenExpiration,
      List<String> redirectUris) {
    Optional<ApplicationEntity> existing = applicationRepository.findByUuid(uuid);
    if (existing.isEmpty()) {
      return Optional.empty();
    }
    ApplicationEntity entity = existing.get();
    entity.setClientType(clientType);
    entity.setName(name);
    entity.setDescription(description);
    entity.setLogo(logo);
    entity.setIdTokenExpiration(idTokenExpiration);
    entity.setRefreshTokenExpiration(refreshTokenExpiration);
    entity.setRedirectUris(String.join(",", redirectUris));
    return Optional.of(applicationRepository.update(entity));
  }

  public boolean deleteByUuid(String uuid) {
    Optional<ApplicationEntity> existing = applicationRepository.findByUuid(uuid);
    if (existing.isEmpty()) {
      return false;
    }
    applicationRepository.delete(existing.get());
    return true;
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

  public List<String> normalizeRedirectUris(List<String> redirectUris) {
    if (redirectUris == null) {
      return List.of();
    }
    return redirectUris.stream()
        .map(value -> value == null ? "" : value.trim())
        .filter(value -> !value.isBlank())
        .distinct()
        .collect(Collectors.toList());
  }

  private String randomToken(int bytesLength) {
    byte[] bytes = new byte[bytesLength];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String generateUniqueClientId() {
    for (int attempt = 0; attempt < MAX_CLIENT_ID_GENERATION_ATTEMPTS; attempt++) {
      String clientId = clientIdGenerator.generate();
      if (applicationRepository.findByClientId(clientId).isEmpty()) {
        return clientId;
      }
    }
    throw new IllegalStateException("Failed to generate unique clientId");
  }
}
