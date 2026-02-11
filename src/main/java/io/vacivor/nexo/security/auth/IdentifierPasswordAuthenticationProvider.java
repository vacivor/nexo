package io.vacivor.nexo.security.auth;

import io.micronaut.context.annotation.Requires;
import io.vacivor.nexo.security.auth.user.IdentityResolver;
import io.vacivor.nexo.security.auth.user.UserDetails;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;

@Singleton
@Requires(beans = IdentityResolver.class)
public class IdentifierPasswordAuthenticationProvider
    implements AuthenticationProvider<IdentifierPasswordAuthenticationToken> {

  private final IdentityResolver identityResolver;
  private final PasswordEncoder passwordEncoder;

  public IdentifierPasswordAuthenticationProvider(IdentityResolver identityResolver,
      PasswordEncoder passwordEncoder) {
    this.identityResolver = identityResolver;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public boolean supports(AuthenticationToken token) {
    return token instanceof IdentifierPasswordAuthenticationToken;
  }

  @Override
  public Optional<Authentication> authenticate(IdentifierPasswordAuthenticationToken token) {
    Optional<UserDetails> user = identityResolver.resolve(token.getIdentifier());
    if (user.isEmpty()) {
      return Optional.empty();
    }
    UserDetails details = user.get();
    if (!details.isEnabled() || !details.isAccountNonLocked()
        || !details.isAccountNonExpired() || !details.isCredentialsNonExpired()) {
      return Optional.empty();
    }
    if (!passwordEncoder.matches(token.getPassword(), details.getPassword())) {
      return Optional.empty();
    }
    return Optional.of(new SimpleAuthentication(details.getUsername(), details.getAuthorities(), Map.of()));
  }
}
