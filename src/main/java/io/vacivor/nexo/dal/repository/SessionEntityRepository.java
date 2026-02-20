package io.vacivor.nexo.dal.repository;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.vacivor.nexo.dal.entity.SessionEntity;
import java.util.List;

@Repository
public interface SessionEntityRepository extends JpaRepository<SessionEntity, String> {

  @Query(value = "SELECT * FROM sessions ORDER BY last_accessed_time DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
  List<SessionEntity> findPage(int offset, int limit);

  @Query(value = "SELECT COUNT(*) FROM sessions", nativeQuery = true)
  long countAllSessions();
}
