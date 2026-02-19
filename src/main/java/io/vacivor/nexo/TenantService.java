package io.vacivor.nexo;

import io.vacivor.nexo.dal.entity.TenantEntity;
import io.vacivor.nexo.dal.repository.TenantRepository;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class TenantService {

  private final TenantRepository tenantRepository;

  public TenantService(TenantRepository tenantRepository) {
    this.tenantRepository = tenantRepository;
  }

  public TenantEntity createTenant(String name) {
    TenantEntity entity = new TenantEntity();
    entity.setUuid(UUID.randomUUID().toString());
    entity.setName(name);
    return tenantRepository.save(entity);
  }

  public Optional<TenantEntity> findByUuid(String uuid) {
    return tenantRepository.findByUuid(uuid);
  }

  public List<TenantEntity> findAll() {
    return tenantRepository.findAll();
  }

  public Optional<TenantEntity> updateTenant(String uuid, String name) {
    return tenantRepository.findByUuid(uuid).map(existing -> {
      existing.setName(name);
      return tenantRepository.update(existing);
    });
  }
}
