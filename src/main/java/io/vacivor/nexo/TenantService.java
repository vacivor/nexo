package io.vacivor.nexo;

import jakarta.inject.Singleton;

@Singleton
public class TenantService {

  private final TenantRepository tenantRepository;

  public TenantService(TenantRepository tenantRepository) {
    this.tenantRepository = tenantRepository;
  }
}
