package io.vacivor.nexo.security.auth.social;

import io.micronaut.context.annotation.Requires;
import io.vacivor.nexo.security.auth.Authentication;
import io.vacivor.nexo.security.auth.AuthenticationProvider;
import io.vacivor.nexo.security.auth.AuthenticationToken;
import io.vacivor.nexo.security.auth.SimpleAuthentication;
import io.vacivor.nexo.dal.entity.UserEntity;
import io.vacivor.nexo.dal.entity.UserIdentityEntity;
import io.vacivor.nexo.dal.repository.UserIdentityRepository;
import io.vacivor.nexo.dal.repository.UserRepository;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Singleton
@Requires(beans = SocialIdentityResolver.class)
public class SocialAuthenticationProvider implements AuthenticationProvider<SocialAuthenticationToken> {

  private final SocialIdentityResolver socialIdentityResolver;
  private final UserRepository userRepository;
  private final UserIdentityRepository userIdentityRepository;

  public SocialAuthenticationProvider(SocialIdentityResolver socialIdentityResolver,
      UserRepository userRepository,
      UserIdentityRepository userIdentityRepository) {
    this.socialIdentityResolver = socialIdentityResolver;
    this.userRepository = userRepository;
    this.userIdentityRepository = userIdentityRepository;
  }

  @Override
  public boolean supports(AuthenticationToken token) {
    return token instanceof SocialAuthenticationToken;
  }

  @Override
  public Optional<Authentication> authenticate(SocialAuthenticationToken token) {
    Optional<SocialIdentity> identity = socialIdentityResolver.resolve(token);
    if (identity.isEmpty()) {
      return Optional.empty();
    }
    SocialIdentity socialIdentity = identity.get();
    Optional<UserIdentityEntity> existing = userIdentityRepository
        .findByProviderAndProviderUserId(socialIdentity.getProvider(), socialIdentity.getProviderUserId());
    if (existing.isPresent()) {
      return userRepository.findById(existing.get().getUserId())
          .map(user -> new SimpleAuthentication(user.getUsername(), Set.of(), Map.of()));
    }
    UserEntity user = new UserEntity();
    user.setUsername(buildUsername(socialIdentity));
    user.setEmail(socialIdentity.getEmail());
    user.setPhone(socialIdentity.getPhone());
    user = userRepository.save(user);

    UserIdentityEntity identityEntity = new UserIdentityEntity();
    identityEntity.setUserId(user.getId());
    identityEntity.setProvider(socialIdentity.getProvider());
    identityEntity.setProviderUserId(socialIdentity.getProviderUserId());
    identityEntity.setIdentifier(firstNonBlank(socialIdentity.getEmail(),
        socialIdentity.getPhone(), socialIdentity.getUsername()));
    userIdentityRepository.save(identityEntity);

    return Optional.of(new SimpleAuthentication(user.getUsername(), Set.of(), Map.of()));
  }

  private String buildUsername(SocialIdentity socialIdentity) {
    String candidate = firstNonBlank(socialIdentity.getUsername(), socialIdentity.getEmail(),
        socialIdentity.getPhone());
    if (candidate == null) {
      return socialIdentity.getProvider() + "_" + socialIdentity.getProviderUserId();
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
