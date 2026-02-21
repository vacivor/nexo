package io.vacivor.nexo.security.auth.oauth2;

import io.micronaut.context.annotation.Requires;
import io.vacivor.nexo.security.auth.core.Authentication;
import io.vacivor.nexo.security.auth.core.AuthenticationProvider;
import io.vacivor.nexo.security.auth.core.AuthenticationToken;
import io.vacivor.nexo.security.auth.core.SimpleAuthentication;
import io.vacivor.nexo.security.auth.social.SocialIdentity;
import io.vacivor.nexo.dal.entity.UserEntity;
import io.vacivor.nexo.dal.entity.UserIdentityEntity;
import io.vacivor.nexo.dal.repository.UserIdentityRepository;
import io.vacivor.nexo.dal.repository.UserRepository;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Singleton
@Requires(beans = OAuth2IdentityResolver.class)
public class OAuth2AuthenticationProvider implements AuthenticationProvider<OAuth2Authentication> {

  private final OAuth2IdentityResolver identityResolver;
  private final UserRepository userRepository;
  private final UserIdentityRepository userIdentityRepository;

  public OAuth2AuthenticationProvider(OAuth2IdentityResolver identityResolver,
      UserRepository userRepository,
      UserIdentityRepository userIdentityRepository) {
    this.identityResolver = identityResolver;
    this.userRepository = userRepository;
    this.userIdentityRepository = userIdentityRepository;
  }

  @Override
  public boolean supports(AuthenticationToken token) {
    return token instanceof OAuth2Authentication;
  }

  @Override
  public Optional<Authentication> authenticate(OAuth2Authentication token) {
    Optional<SocialIdentity> identityOpt = identityResolver.resolve(token);
    if (identityOpt.isEmpty()) {
      return Optional.empty();
    }
    SocialIdentity identity = identityOpt.get();
    Optional<UserIdentityEntity> existing = userIdentityRepository
        .findByProviderAndProviderUserId(identity.getProvider(), identity.getProviderUserId());
    if (existing.isPresent()) {
      return userRepository.findById(existing.get().getUserId())
          .map(user -> new SimpleAuthentication(user.getUsername(), Set.of(), Map.of()));
    }

    UserEntity user = new UserEntity();
    user.setUsername(buildUsername(identity));
    user.setEmail(identity.getEmail());
    user.setPhone(identity.getPhone());
    user = userRepository.save(user);

    UserIdentityEntity userIdentity = new UserIdentityEntity();
    userIdentity.setUserId(user.getId());
    userIdentity.setProvider(identity.getProvider());
    userIdentity.setProviderUserId(identity.getProviderUserId());
    userIdentity.setIdentifier(firstNonBlank(identity.getEmail(), identity.getPhone(), identity.getUsername()));
    userIdentityRepository.save(userIdentity);

    return Optional.of(new SimpleAuthentication(user.getUsername(), Set.of(), Map.of()));
  }

  private String buildUsername(SocialIdentity identity) {
    String candidate = firstNonBlank(identity.getUsername(), identity.getEmail(), identity.getPhone());
    if (candidate == null) {
      return identity.getProvider() + "_" + identity.getProviderUserId();
    }
    return candidate;
  }

  private String firstNonBlank(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return null;
  }
}
