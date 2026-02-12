package io.vacivor.nexo.dal.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "tenants")
public class TenantEntity extends BaseEntity {

  @Column(name = "uuid", nullable = false, unique = true, updatable = false)
  private String uuid;
  @Column(name = "name")
  private String name;

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
