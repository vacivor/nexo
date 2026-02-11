package io.vacivor.nexo;


import jakarta.persistence.Column;

public class TenantEntity extends BaseEntity {

  @Column(name = "uuid")
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
