package io.vacivor.nexo.security.auth;

import io.micronaut.context.annotation.Primary;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.context.ServerRequestContext;
import jakarta.inject.Singleton;

@Singleton
@Primary
public class DelegatingAuthenticationPersistence implements AuthenticationPersistence {

  private final ClientTypeResolver clientTypeResolver;
  private final SessionAuthenticationPersistence sessionAuthenticationPersistence;
  private final TokenAuthenticationPersistence tokenAuthenticationPersistence;

  public DelegatingAuthenticationPersistence(ClientTypeResolver clientTypeResolver,
      SessionAuthenticationPersistence sessionAuthenticationPersistence,
      TokenAuthenticationPersistence tokenAuthenticationPersistence) {
    this.clientTypeResolver = clientTypeResolver;
    this.sessionAuthenticationPersistence = sessionAuthenticationPersistence;
    this.tokenAuthenticationPersistence = tokenAuthenticationPersistence;
  }

  @Override
  public void onSuccess(Authentication authentication, MutableHttpResponse<?> response) {
    ClientType clientType = ServerRequestContext.currentRequest()
        .map(clientTypeResolver::resolve)
        .orElse(ClientType.WEB);
    if (clientType == ClientType.MOBILE) {
      tokenAuthenticationPersistence.onSuccess(authentication, response);
      return;
    }
    sessionAuthenticationPersistence.onSuccess(authentication, response);
  }

  @Override
  public void onFailure(MutableHttpResponse<?> response) {
    sessionAuthenticationPersistence.onFailure(response);
    tokenAuthenticationPersistence.onFailure(response);
  }
}
