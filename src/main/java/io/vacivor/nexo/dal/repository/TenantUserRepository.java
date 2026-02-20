package io.vacivor.nexo.dal.repository;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.vacivor.nexo.dal.entity.TenantUserEntity;

@Repository
public interface TenantUserRepository extends JpaRepository<TenantUserEntity, Long> {

  @Query("""
      SELECT COUNT(tu) > 0
      FROM TenantUserEntity tu
      WHERE tu.userId = :userId
        AND tu.tenantId = :tenantId
        AND (tu.isDeleted IS NULL OR tu.isDeleted = false)
      """)
  boolean existsActiveMembership(Long userId, String tenantId);
}
