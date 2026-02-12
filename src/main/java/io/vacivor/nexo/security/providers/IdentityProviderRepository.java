package io.vacivor.nexo.security.providers;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import java.util.Optional;

@Repository
public interface IdentityProviderRepository extends JpaRepository<IdentityProviderEntity, Long> {

  Optional<IdentityProviderEntity> findByUuid(String uuid);

  Optional<IdentityProviderEntity> findByProtocolAndProviderAndEnabledTrue(
      IdentityProviderProtocol protocol, String provider);
}
