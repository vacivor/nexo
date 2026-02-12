package io.vacivor.nexo.dal.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.vacivor.nexo.dal.entity.UserEntity;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

  Optional<UserEntity> findByUsername(String username);

  Optional<UserEntity> findByPhone(String phone);

  Optional<UserEntity> findByEmail(String email);

  List<UserEntity> findByIsDeletedNotTrue();

  Optional<UserEntity> findByIdAndIsDeletedNotTrue(Long id);
}
