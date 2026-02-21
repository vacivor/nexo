package io.vacivor.nexo.security.auth.oidc;

import java.util.Optional;

public interface OidcIdentityResolver {

  Optional<OidcIdentity> resolve(OidcAuthenticationToken token);
}
