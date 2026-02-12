package io.vacivor.nexo;

import io.vacivor.nexo.dal.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class TenantBaseEntity extends BaseEntity {

  @Column(name = "tenant_id")
  private String tenantId;

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }
}
