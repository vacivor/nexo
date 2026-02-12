package io.vacivor.nexo.dal.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.vacivor.nexo.dal.entity.UserIdentityEntity;
import java.util.Optional;

@Repository
public interface UserIdentityRepository extends JpaRepository<UserIdentityEntity, Long> {

  Optional<UserIdentityEntity> findByProviderAndProviderUserId(String provider, String providerUserId);
}
