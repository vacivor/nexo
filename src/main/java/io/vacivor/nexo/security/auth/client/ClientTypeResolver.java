package io.vacivor.nexo.security.auth.client;

import io.micronaut.http.HttpRequest;

public interface ClientTypeResolver {

  ClientType resolve(HttpRequest<?> request);
}
