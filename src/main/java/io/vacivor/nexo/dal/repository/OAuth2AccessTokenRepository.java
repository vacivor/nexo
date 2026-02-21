package io.vacivor.nexo.dal.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.vacivor.nexo.dal.entity.OAuth2AccessTokenEntity;
import java.util.List;
import java.util.Optional;

@Repository
public interface OAuth2AccessTokenRepository extends JpaRepository<OAuth2AccessTokenEntity, Long> {

  Optional<OAuth2AccessTokenEntity> findByToken(String token);

  List<OAuth2AccessTokenEntity> findByFamilyId(String familyId);
}
