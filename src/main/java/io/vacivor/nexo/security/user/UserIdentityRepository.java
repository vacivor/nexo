package io.vacivor.nexo.security.user;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import java.util.Optional;

@Repository
public interface UserIdentityRepository extends JpaRepository<UserIdentityEntity, Long> {

  Optional<UserIdentityEntity> findByProviderAndProviderUserId(String provider, String providerUserId);
}
