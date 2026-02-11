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
    fixation-strategy: MIGRATE   # MIGRATE | NEW | NONE
```

Notes:
- `store` selects the active session repository implementation.
- `header-name` is used for mobile/API clients to send the session id.
- `cookie-name` is used for browser clients.
- `fixation-strategy` controls how session ids are handled on login.
- Client type routing for login uses request header `X-Client-Type: mobile` to return a token in header instead of cookie.

