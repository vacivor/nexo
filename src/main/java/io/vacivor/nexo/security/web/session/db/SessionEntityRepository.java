package io.vacivor.nexo.security.web.session.db;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

@Repository
public interface SessionEntityRepository extends JpaRepository<SessionEntity, String> {
}
