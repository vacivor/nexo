# Spring Security 接入示例（OIDC Login）

这是一个最小可运行示例：Spring Boot + Spring Security OAuth2 Client，接入当前 `nexo` 的 OIDC 能力。

## 1. 前置条件

- 先启动 `nexo`（默认 `http://localhost:8080`）。
- 确保 OIDC/OAuth2 端点已开启（`nexo` 配置里）：
  - `nexo.oidc.enabled=true`
  - `nexo.oauth2.enabled=true`
- 在 `nexo` 创建一个 `application/client`，并配置回调地址：
  - `http://localhost:9090/login/oauth2/code/nexo`

## 2. 配置客户端

编辑 `/Users/valurno/nexo/examples/spring-security-client/src/main/resources/application.yml`：

- `spring.security.oauth2.client.registration.nexo.client-id`
- `spring.security.oauth2.client.registration.nexo.client-secret`

`issuer-uri` 默认是 `http://localhost:8080`，会自动读取：

- `http://localhost:8080/.well-known/openid-configuration`

## 3. 启动示例

在 `/Users/valurno/nexo/examples/spring-security-client` 目录执行：

```bash
mvn spring-boot:run
```

打开：

- `http://localhost:9090`

点击 `Login with Nexo OIDC`，完成登录后可访问：

- `/me`（查看 Spring Security 中的当前认证信息和 OIDC claims）

## 4. 关键代码

- 启动类：`/Users/valurno/nexo/examples/spring-security-client/src/main/java/io/vacivor/nexo/examples/springclient/SpringSecurityClientExampleApplication.java`
- 安全配置：`/Users/valurno/nexo/examples/spring-security-client/src/main/java/io/vacivor/nexo/examples/springclient/SecurityConfig.java`
- 示例接口：`/Users/valurno/nexo/examples/spring-security-client/src/main/java/io/vacivor/nexo/examples/springclient/HomeController.java`
