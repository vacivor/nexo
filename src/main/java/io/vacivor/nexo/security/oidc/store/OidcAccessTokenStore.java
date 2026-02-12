package io.vacivor.nexo.security.oidc.store;

import io.vacivor.nexo.security.oidc.OidcAccessToken;
import java.util.Optional;

public interface OidcAccessTokenStore {

  void store(OidcAccessToken token);

  Optional<OidcAccessToken> find(String token);
}
