package io.vacivor.nexo.dal.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.vacivor.nexo.dal.entity.OAuth2RefreshTokenEntity;
import java.util.List;
import java.util.Optional;

@Repository
public interface OAuth2RefreshTokenRepository extends JpaRepository<OAuth2RefreshTokenEntity, Long> {

  Optional<OAuth2RefreshTokenEntity> findByToken(String token);

  List<OAuth2RefreshTokenEntity> findByFamilyId(String familyId);
}
