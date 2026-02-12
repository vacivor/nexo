package io.vacivor.nexo.dal.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.vacivor.nexo.dal.entity.TenantEntity;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<TenantEntity, Long> {

  Optional<TenantEntity> findByUuid(String uuid);
}
