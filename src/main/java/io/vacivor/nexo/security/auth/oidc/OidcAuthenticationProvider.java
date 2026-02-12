package io.vacivor.nexo.security.auth.oidc;

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
@Requires(beans = OidcIdentityResolver.class)
public class OidcAuthenticationProvider implements AuthenticationProvider<OidcAuthenticationToken> {

  private static final String PROVIDER_PREFIX = "oidc:";

  private final OidcIdentityResolver identityResolver;
  private final UserRepository userRepository;
  private final UserIdentityRepository userIdentityRepository;

  public OidcAuthenticationProvider(OidcIdentityResolver identityResolver,
      UserRepository userRepository,
      UserIdentityRepository userIdentityRepository) {
    this.identityResolver = identityResolver;
    this.userRepository = userRepository;
    this.userIdentityRepository = userIdentityRepository;
  }

  @Override
  public boolean supports(AuthenticationToken token) {
    return token instanceof OidcAuthenticationToken;
  }

  @Override
  public Optional<Authentication> authenticate(OidcAuthenticationToken token) {
    Optional<OidcIdentity> resolved = identityResolver.resolve(token);
    if (resolved.isEmpty()) {
      return Optional.empty();
    }
    OidcIdentity identity = resolved.get();
    String provider = toProvider(identity.getProvider());
    Optional<UserIdentityEntity> existing = userIdentityRepository
        .findByProviderAndProviderUserId(provider, identity.getSubject());
    if (existing.isPresent()) {
      return userRepository.findById(existing.get().getUserId())
          .map(user -> new SimpleAuthentication(user.getUsername(), Set.of(), Map.of()));
    }

    UserEntity user = new UserEntity();
    user.setUsername(buildUsername(identity));
    user.setEmail(identity.getEmail());
    user.setPhone(identity.getPhone());
    user = userRepository.save(user);

    UserIdentityEntity identityEntity = new UserIdentityEntity();
    identityEntity.setUserId(user.getId());
    identityEntity.setProvider(provider);
    identityEntity.setProviderUserId(identity.getSubject());
    identityEntity.setIdentifier(firstNonBlank(identity.getEmail(), identity.getPhone(), identity.getUsername()));
    userIdentityRepository.save(identityEntity);

    return Optional.of(new SimpleAuthentication(user.getUsername(), Set.of(), Map.of()));
  }

  private String toProvider(String provider) {
    if (provider == null || provider.isBlank()) {
      return PROVIDER_PREFIX + "default";
    }
    if (provider.startsWith(PROVIDER_PREFIX)) {
      return provider;
    }
    return PROVIDER_PREFIX + provider;
  }

  private String buildUsername(OidcIdentity identity) {
    String candidate = firstNonBlank(identity.getUsername(), identity.getEmail(), identity.getPhone());
    if (candidate == null) {
      return toProvider(identity.getProvider()) + "_" + identity.getSubject();
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
