package io.vacivor.nexo.dal.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.vacivor.nexo.dal.entity.IdentityProviderEntity;
import io.vacivor.nexo.security.providers.IdentityProviderProtocol;
import java.util.Optional;

@Repository
public interface IdentityProviderRepository extends JpaRepository<IdentityProviderEntity, Long> {

  Optional<IdentityProviderEntity> findByUuid(String uuid);

  Optional<IdentityProviderEntity> findByProtocolAndProviderAndEnabledTrue(
      IdentityProviderProtocol protocol, String provider);
}
