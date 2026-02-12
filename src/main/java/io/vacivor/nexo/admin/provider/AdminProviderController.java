package io.vacivor.nexo.admin.provider;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Put;
import io.micronaut.serde.annotation.Serdeable;
import io.vacivor.nexo.security.auth.social.SocialProvider;
import io.vacivor.nexo.dal.entity.IdentityProviderEntity;
import io.vacivor.nexo.security.providers.IdentityProviderProtocol;
import io.vacivor.nexo.security.providers.IdentityProviderService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller("/api/admin/providers")
public class AdminProviderController {

  private final IdentityProviderService service;

  public AdminProviderController(IdentityProviderService service) {
    this.service = service;
  }

  @Post
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> create(@Body AdminProviderRequest request) {
    if (!isValid(request)) {
      return HttpResponse.badRequest(Map.of("error", "invalid_request"));
    }
    IdentityProviderEntity created = service.create(toEntity(request));
    return HttpResponse.ok(toResponse(created));
  }

  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<List<AdminProviderResponse>> list() {
    return HttpResponse.ok(service.findAll().stream().map(this::toResponse).collect(Collectors.toList()));
  }

  @Get("/{uuid}")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> get(@PathVariable String uuid) {
    return service.findByUuid(uuid)
        .<HttpResponse<?>>map(entity -> HttpResponse.ok(toResponse(entity)))
        .orElseGet(HttpResponse::notFound);
  }

  @Put("/{uuid}")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> update(@PathVariable String uuid, @Body AdminProviderRequest request) {
    if (!isValid(request)) {
      return HttpResponse.badRequest(Map.of("error", "invalid_request"));
    }
    return service.update(uuid, toEntity(request))
        .<HttpResponse<?>>map(entity -> HttpResponse.ok(toResponse(entity)))
        .orElseGet(HttpResponse::notFound);
  }

  @Patch("/{uuid}/enabled/{enabled}")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<?> setEnabled(@PathVariable String uuid, @PathVariable boolean enabled) {
    return service.setEnabled(uuid, enabled)
        .<HttpResponse<?>>map(entity -> HttpResponse.ok(toResponse(entity)))
        .orElseGet(HttpResponse::notFound);
  }

  @Delete("/{uuid}")
  public HttpResponse<?> delete(@PathVariable String uuid) {
    return service.deleteByUuid(uuid) ? HttpResponse.noContent() : HttpResponse.notFound();
  }

  private boolean isValid(AdminProviderRequest request) {
    if (request == null || request.protocol() == null
        || request.provider() == null || request.provider().isBlank()) {
      return false;
    }
    if (request.protocol() != IdentityProviderProtocol.SOCIAL) {
      return true;
    }
    return SocialProvider.from(request.provider()).isPresent();
  }

  private IdentityProviderEntity toEntity(AdminProviderRequest request) {
    IdentityProviderEntity entity = new IdentityProviderEntity();
    entity.setProtocol(request.protocol());
    entity.setProvider(request.provider());
    entity.setDisplayName(request.displayName());
    entity.setEnabled(request.enabled());
    entity.setClientId(request.clientId());
    entity.setClientSecret(request.clientSecret());
    entity.setAuthorizationUri(request.authorizationUri());
    entity.setTokenUri(request.tokenUri());
    entity.setUserInfoUri(request.userInfoUri());
    entity.setJwksUri(request.jwksUri());
    entity.setIssuer(request.issuer());
    entity.setRedirectUri(request.redirectUri());
    entity.setScopes(request.scopes());
    entity.setExtraConfig(request.extraConfig());
    return entity;
  }

  private AdminProviderResponse toResponse(IdentityProviderEntity entity) {
    return new AdminProviderResponse(
        entity.getId(),
        entity.getUuid(),
        entity.getProtocol(),
        entity.getProvider(),
        entity.getDisplayName(),
        Boolean.TRUE.equals(entity.getEnabled()),
        entity.getClientId(),
        entity.getAuthorizationUri(),
        entity.getTokenUri(),
        entity.getUserInfoUri(),
        entity.getJwksUri(),
        entity.getIssuer(),
        entity.getRedirectUri(),
        entity.getScopes(),
        entity.getExtraConfig()
    );
  }

  @Introspected
  @Serdeable.Deserializable
  public record AdminProviderRequest(
      IdentityProviderProtocol protocol,
      String provider,
      String displayName,
      Boolean enabled,
      String clientId,
      String clientSecret,
      String authorizationUri,
      String tokenUri,
      String userInfoUri,
      String jwksUri,
      String issuer,
      String redirectUri,
      String scopes,
      String extraConfig
  ) {
  }

  @Introspected
  @Serdeable
  public record AdminProviderResponse(
      Long id,
      String uuid,
      IdentityProviderProtocol protocol,
      String provider,
      String displayName,
      boolean enabled,
      String clientId,
      String authorizationUri,
      String tokenUri,
      String userInfoUri,
      String jwksUri,
      String issuer,
      String redirectUri,
      String scopes,
      String extraConfig
  ) {
  }
}
