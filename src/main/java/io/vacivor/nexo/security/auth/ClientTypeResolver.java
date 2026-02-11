package io.vacivor.nexo.security.auth;

import io.micronaut.http.HttpRequest;

public interface ClientTypeResolver {

  ClientType resolve(HttpRequest<?> request);
}
