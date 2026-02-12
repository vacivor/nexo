package io.vacivor.nexo.security.oidc;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import java.util.Optional;

@Repository
public interface OidcConsentRepository extends JpaRepository<OidcConsentEntity, Long> {

  Optional<OidcConsentEntity> findBySubjectAndClientId(String subject, String clientId);
}
