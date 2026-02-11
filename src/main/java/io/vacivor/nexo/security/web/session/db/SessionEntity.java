package io.vacivor.nexo.security.web.session.db;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Serdeable
@Entity
@Table(name = "sessions")
public class SessionEntity {

  @Id
  @Column(name = "id", nullable = false)
  private String id;

  @Column(name = "creation_time", nullable = false)
  private Instant creationTime;

  @Column(name = "last_accessed_time", nullable = false)
  private Instant lastAccessedTime;

  @Column(name = "max_inactive_interval_seconds")
  private Long maxInactiveIntervalSeconds;

  @Column(name = "is_new", nullable = false)
  private Boolean isNew;

  @Lob
  @Column(name = "attributes_json")
  private String attributesJson;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Instant getCreationTime() {
    return creationTime;
  }

  public void setCreationTime(Instant creationTime) {
    this.creationTime = creationTime;
  }

  public Instant getLastAccessedTime() {
    return lastAccessedTime;
  }

  public void setLastAccessedTime(Instant lastAccessedTime) {
    this.lastAccessedTime = lastAccessedTime;
  }

  public Long getMaxInactiveIntervalSeconds() {
    return maxInactiveIntervalSeconds;
  }

  public void setMaxInactiveIntervalSeconds(Long maxInactiveIntervalSeconds) {
    this.maxInactiveIntervalSeconds = maxInactiveIntervalSeconds;
  }

  public Boolean getNew() {
    return isNew;
  }

  public void setNew(Boolean isNew) {
    this.isNew = isNew;
  }

  public String getAttributesJson() {
    return attributesJson;
  }

  public void setAttributesJson(String attributesJson) {
    this.attributesJson = attributesJson;
  }
}
