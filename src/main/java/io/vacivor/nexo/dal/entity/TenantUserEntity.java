package io.vacivor.nexo.dal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "tenant_users",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_tenant_users_user_tenant",
            columnNames = {"user_id", "tenant_id"})
    })
public class TenantUserEntity extends BaseEntity {

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }
}
