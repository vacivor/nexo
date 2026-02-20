package io.vacivor.nexo.security.auth.service;

import io.vacivor.nexo.security.auth.core.*;
import io.vacivor.nexo.security.auth.service.*;
import io.vacivor.nexo.security.auth.persistence.*;
import io.vacivor.nexo.security.auth.handler.*;
import io.vacivor.nexo.security.auth.password.*;
import io.vacivor.nexo.security.auth.provider.local.*;
import io.vacivor.nexo.security.auth.client.*;
import io.vacivor.nexo.security.auth.dto.*;
import io.vacivor.nexo.security.auth.web.*;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.vacivor.nexo.dal.entity.UserEntity;
import io.vacivor.nexo.dal.repository.UserRepository;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class RegistrationService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public MutableHttpResponse<RegisterResponse> register(RegisterRequest request) {
    if (isBlank(request.getUsername())) {
      return HttpResponse.badRequest();
    }
    if (isBlank(request.getPassword())) {
      return HttpResponse.badRequest();
    }

    if (!isBlank(request.getUsername())) {
      Optional<UserEntity> existing = userRepository.findByUsername(request.getUsername());
      if (existing.isPresent()) {
        return HttpResponse.status(HttpStatus.CONFLICT);
      }
    }
    UserEntity user = new UserEntity();
    user.setUsername(trimToNull(request.getUsername()));
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user = userRepository.save(user);

    return HttpResponse.created(new RegisterResponse(user.getId(), user.getUsername(), user.getEmail(), user.getPhone()));
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}