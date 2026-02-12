package io.vacivor.nexo.dal.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.vacivor.nexo.dal.entity.ApplicationEntity;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {

  Optional<ApplicationEntity> findByClientId(String clientId);

  Optional<ApplicationEntity> findByUuid(String uuid);

}
