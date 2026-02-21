package io.vacivor.nexo.dal.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.vacivor.nexo.dal.entity.OidcAccessTokenEntity;
import java.util.List;
import java.util.Optional;

@Repository
public interface OidcAccessTokenRepository extends JpaRepository<OidcAccessTokenEntity, Long> {

  Optional<OidcAccessTokenEntity> findByToken(String token);

  List<OidcAccessTokenEntity> findByFamilyId(String familyId);
}
