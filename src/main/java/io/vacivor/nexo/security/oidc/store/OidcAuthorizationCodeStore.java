package io.vacivor.nexo.security.oidc.store;

import io.vacivor.nexo.security.oidc.OidcAuthorizationCode;
import java.util.Optional;

public interface OidcAuthorizationCodeStore {

  void store(OidcAuthorizationCode code);

  Optional<OidcAuthorizationCode> consume(String code);
}
