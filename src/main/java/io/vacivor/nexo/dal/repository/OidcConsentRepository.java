package io.vacivor.nexo.dal.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.vacivor.nexo.dal.entity.OidcConsentEntity;
import java.util.Optional;

@Repository
public interface OidcConsentRepository extends JpaRepository<OidcConsentEntity, Long> {

  Optional<OidcConsentEntity> findBySubjectAndClientId(String subject, String clientId);
}
