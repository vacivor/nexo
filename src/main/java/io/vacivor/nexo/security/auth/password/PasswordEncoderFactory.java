package io.vacivor.nexo.security.auth.password;

import io.vacivor.nexo.security.auth.core.*;
import io.vacivor.nexo.security.auth.service.*;
import io.vacivor.nexo.security.auth.persistence.*;
import io.vacivor.nexo.security.auth.handler.*;
import io.vacivor.nexo.security.auth.password.*;
import io.vacivor.nexo.security.auth.provider.local.*;
import io.vacivor.nexo.security.auth.client.*;
import io.vacivor.nexo.security.auth.dto.*;
import io.vacivor.nexo.security.auth.web.*;

import io.micronaut.context.annotation.Factory;
import io.vacivor.nexo.security.config.SecurityConfiguration;
import jakarta.inject.Singleton;
import java.util.Locale;

@Factory
public class PasswordEncoderFactory {

  @Singleton
  public PasswordEncoder passwordEncoder(SecurityConfiguration securityConfiguration) {
    String configured = securityConfiguration.getPasswordEncoder();
    String key = configured == null ? "plain" : configured.trim().toLowerCase(Locale.ROOT);
    return switch (key) {
      case "bcrypt" -> new BcryptPasswordEncoder();
      case "plain" -> new PlainTextPasswordEncoder();
      default -> throw new IllegalArgumentException(
          "Unsupported nexo.security.password-encoder: " + configured
              + " (supported: bcrypt, plain)");
    };
  }
}