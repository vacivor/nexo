package io.vacivor.nexo.security.auth.social;

import java.util.Optional;

public interface SocialIdentityResolver {

  Optional<SocialIdentity> resolve(SocialAuthenticationToken token);
}
