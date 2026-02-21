package io.vacivor.nexo.dal.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.vacivor.nexo.dal.entity.OidcRefreshTokenEntity;
import java.util.List;
import java.util.Optional;

@Repository
public interface OidcRefreshTokenRepository extends JpaRepository<OidcRefreshTokenEntity, Long> {

  Optional<OidcRefreshTokenEntity> findByToken(String token);

  List<OidcRefreshTokenEntity> findByFamilyId(String familyId);
}
