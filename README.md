## Micronaut 4.10.8 Documentation

- [User Guide](https://docs.micronaut.io/4.10.8/guide/index.html)
- [API Reference](https://docs.micronaut.io/4.10.8/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/4.10.8/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)

---

- [Shadow Gradle Plugin](https://gradleup.com/shadow/)
- [Micronaut Gradle Plugin documentation](https://micronaut-projects.github.io/micronaut-gradle-plugin/latest/)
- [GraalVM Gradle Plugin documentation](https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html)

## Feature undertow-server documentation

- [Micronaut Undertow Server documentation](https://micronaut-projects.github.io/micronaut-servlet/latest/guide/index.html#undertow)

## Feature jdbc-hikari documentation

- [Micronaut Hikari JDBC Connection Pool documentation](https://micronaut-projects.github.io/micronaut-sql/latest/guide/index.html#jdbc)

## Feature micronaut-aot documentation

- [Micronaut AOT documentation](https://micronaut-projects.github.io/micronaut-aot/latest/guide/)

## Feature test-resources documentation

- [Micronaut Test Resources documentation](https://micronaut-projects.github.io/micronaut-test-resources/latest/guide/)

## Feature serialization-jackson documentation

- [Micronaut Serialization Jackson Core documentation](https://micronaut-projects.github.io/micronaut-serialization/latest/guide/)

## Security Session Configuration

The session and authentication flow is controlled by `nexo.session.*` config.

```yaml
nexo:
  session:
    store: map            # map | inmemory | redis | db
    max-inactive-interval: 30m
    in-memory-maximum-size: 10000
    redis-key-prefix: "nexo:sessions:"
    cookie-name: "NEXO_SESSION"
    header-name: "X-Session-Id"
    cookie-transport-enabled: true
    header-transport-enabled: true
    cookie-secure: false
    cookie-same-site: "LAX"      # LAX | STRICT | NONE
    fixation-strategy: MIGRATE   # MIGRATE | NEW | NONE
  oidc:
    issuer: "http://localhost:8080"
    hmac-secret: "change-me"
    code-ttl: 5m
    access-token-ttl: 1h
    id-token-ttl: 1h
```

Notes:
- `store` selects the active session repository implementation.
- `header-name` is used for mobile/API clients to send the session id.
- `cookie-name` is used for browser clients.
- `cookie-transport-enabled` / `header-transport-enabled` control which transport channels are accepted.
- `cookie-secure` and `cookie-same-site` harden browser cookie behavior.
- `fixation-strategy` controls how session ids are handled on login.
- Client type routing for login uses request header `X-Client-Type: mobile` to return a token in header instead of cookie.
- OIDC uses `nexo.oidc.*` and issues HS256 signed ID tokens with the configured `hmac-secret`.
- OIDC redirect URI validation is strict: absolute URI only, no fragment, exact string match against registered redirects.
- OIDC consent flow:
  - First authorize request redirects to `/oidc/consent` when consent is missing.
  - Consent submit endpoints are `POST /oidc/consent/approve` and `POST /oidc/consent/deny`.
  - CSRF is enforced with one-time `request_id + csrf_token` stored in server session.

## Login Endpoints

- Local login: `POST /login` with `{ "identifier": "...", "password": "..." }`
- Register: `POST /register`
- OIDC login: `POST /login/oidc` with `{ "provider": "xxx", "idToken": "..." }`
- Social access-token login: `POST /login/social` with `{ "provider": "xxx", "accessToken": "..." }`
- OAuth2 code login: `POST /login/oauth2` with `{ "provider": "xxx", "code": "...", "redirectUri": "..." }`

Notes:
- `POST /login/oidc` has a default resolver that validates HS256 `id_token` using `nexo.oidc.hmac-secret`.
- Social and OAuth2 login are extension points. Implement `SocialIdentityResolver` and/or `OAuth2IdentityResolver` to resolve provider identity in production.
