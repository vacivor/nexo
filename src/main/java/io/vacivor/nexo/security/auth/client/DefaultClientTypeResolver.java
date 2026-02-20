package io.vacivor.nexo.security.auth.client;

import io.vacivor.nexo.security.auth.core.*;
import io.vacivor.nexo.security.auth.service.*;
import io.vacivor.nexo.security.auth.persistence.*;
import io.vacivor.nexo.security.auth.handler.*;
import io.vacivor.nexo.security.auth.password.*;
import io.vacivor.nexo.security.auth.provider.local.*;
import io.vacivor.nexo.security.auth.client.*;
import io.vacivor.nexo.security.auth.dto.*;
import io.vacivor.nexo.security.auth.web.*;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import jakarta.inject.Singleton;

@Singleton
@Requires(missingBeans = ClientTypeResolver.class)
public class DefaultClientTypeResolver implements ClientTypeResolver {

  private static final String HEADER_NAME = "X-Client-Type";

  @Override
  public ClientType resolve(HttpRequest<?> request) {
    String value = request.getHeaders().get(HEADER_NAME);
    if (value == null) {
      return ClientType.WEB;
    }
    if ("mobile".equalsIgnoreCase(value)) {
      return ClientType.MOBILE;
    }
    return ClientType.WEB;
  }
}