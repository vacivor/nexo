package io.vacivor.nexo.exception;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micronaut.serde.annotation.Serdeable;
import java.util.LinkedHashMap;
import java.util.Map;

@Serdeable
public class ProblemDetails {

  private String type;
  private String title;
  private int status;
  private String detail;
  private String instance;

  @JsonIgnore
  private final Map<String, Object> properties = new LinkedHashMap<>();

  public ProblemDetails() {
  }

  public ProblemDetails(String type, String title, int status, String detail, String instance) {
    this.type = type;
    this.title = title;
    this.status = status;
    this.detail = detail;
    this.instance = instance;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public String getInstance() {
    return instance;
  }

  public void setInstance(String instance) {
    this.instance = instance;
  }

  @JsonAnyGetter
  public Map<String, Object> getProperties() {
    return properties;
  }

  @JsonAnySetter
  public void addProperty(String name, Object value) {
    if (name == null || name.isBlank()) {
      return;
    }
    properties.put(name, value);
  }

  public ProblemDetails withProperty(String name, Object value) {
    addProperty(name, value);
    return this;
  }
}
