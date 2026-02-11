package io.vacivor.nexo.security.auth;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class RegisterResponse {

  private Long userId;
  private String username;
  private String email;
  private String phone;

  public RegisterResponse() {
  }

  public RegisterResponse(Long userId, String username, String email, String phone) {
    this.userId = userId;
    this.username = username;
    this.email = email;
    this.phone = phone;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }
}
