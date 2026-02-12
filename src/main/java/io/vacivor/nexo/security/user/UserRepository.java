package io.vacivor.nexo.security.user;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

  Optional<UserEntity> findByUsername(String username);

  Optional<UserEntity> findByPhone(String phone);

  Optional<UserEntity> findByEmail(String email);

  List<UserEntity> findByDeletedNotTrue();

  Optional<UserEntity> findByIdAndDeletedNotTrue(Long id);
}
