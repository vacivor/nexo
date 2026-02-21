package io.vacivor.nexo.security.auth.oauth2;

import io.vacivor.nexo.security.auth.social.SocialIdentity;
import java.util.Optional;

public interface OAuth2IdentityResolver {

  Optional<SocialIdentity> resolve(OAuth2Authentication token);
}
