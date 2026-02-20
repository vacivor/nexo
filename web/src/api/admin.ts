import { callApi } from './http'
import type { Application, IdentityProvider, IdentityProviderProtocol, LoginResponse, SessionView, Tenant, User } from './types'
import { resolveConsoleApiBase } from '../layout/consoleScope'

function apiPath(path: string): string {
  return `${resolveConsoleApiBase()}${path}`
}

export async function login(identifier: string, password: string): Promise<LoginResponse | undefined> {
  return callApi<LoginResponse>('/login', 'POST', { identifier, password })
}

export async function register(username: string, password: string): Promise<void> {
  await callApi('/register', 'POST', { username, password })
}

export async function loginSocial(provider: string, accessToken: string): Promise<LoginResponse | undefined> {
  return callApi<LoginResponse>('/login/social', 'POST', { provider, accessToken })
}

export async function loginOidc(provider: string, idToken: string): Promise<LoginResponse | undefined> {
  return callApi<LoginResponse>('/login/oidc', 'POST', { provider, idToken })
}

export async function loginOauth2(provider: string, code: string, redirectUri: string): Promise<LoginResponse | undefined> {
  return callApi<LoginResponse>('/login/oauth2', 'POST', { provider, code, redirectUri })
}

export async function listUsers(): Promise<User[]> {
  return (await callApi<User[]>(apiPath('/users'), 'GET')) ?? []
}

export async function getUser(userId: string): Promise<User | undefined> {
  return callApi<User>(apiPath(`/users/${userId}`), 'GET')
}

export async function createUser(input: {
  username: string
  email?: string
  phone?: string
  password: string
}): Promise<void> {
  await callApi(apiPath('/users'), 'POST', input)
}

export async function updateUser(userId: string, input: {
  username: string
  email?: string
  phone?: string
}): Promise<void> {
  await callApi(apiPath(`/users/${userId}`), 'PUT', input)
}

export async function resetUserPassword(userId: string, password: string): Promise<void> {
  await callApi(apiPath(`/users/${userId}/password`), 'PATCH', { password })
}

export async function deleteUser(userId: number): Promise<void> {
  await callApi(apiPath(`/users/${userId}`), 'DELETE')
}

export async function listTenants(): Promise<Tenant[]> {
  return (await callApi<Tenant[]>(apiPath('/tenants'), 'GET')) ?? []
}

export async function getTenant(uuid: string): Promise<Tenant | undefined> {
  return callApi<Tenant>(apiPath(`/tenants/${uuid}`), 'GET')
}

export async function createTenant(name: string): Promise<void> {
  await callApi(apiPath('/tenants'), 'POST', { name })
}

export async function updateTenant(uuid: string, name: string): Promise<void> {
  await callApi(apiPath(`/tenants/${uuid}`), 'PUT', { name })
}

export async function createApplication(input: {
  clientType: string
  name: string
  description?: string
}): Promise<Application | undefined> {
  return callApi<Application>(apiPath('/applications'), 'POST', input)
}

export async function listApplications(): Promise<Application[]> {
  return (await callApi<Application[]>(apiPath('/applications'), 'GET')) ?? []
}

export async function getApplication(uuid: string): Promise<Application | undefined> {
  return callApi<Application>(apiPath(`/applications/${uuid}`), 'GET')
}

export async function updateApplication(uuid: string, input: {
  tenantId?: string
  clientType?: string
  name?: string
  description?: string
  logo?: string
  idTokenExpiration?: number
  refreshTokenExpiration?: number
  redirectUris?: string[]
}): Promise<Application | undefined> {
  return callApi<Application>(apiPath(`/applications/${uuid}`), 'PUT', input)
}

export async function deleteApplication(uuid: string): Promise<void> {
  await callApi(apiPath(`/applications/${uuid}`), 'DELETE')
}

export async function listProviders(): Promise<IdentityProvider[]> {
  return (await callApi<IdentityProvider[]>(apiPath('/providers'), 'GET')) ?? []
}

export async function getProvider(uuid: string): Promise<IdentityProvider | undefined> {
  return callApi<IdentityProvider>(apiPath(`/providers/${uuid}`), 'GET')
}

export async function createProvider(input: {
  protocol: IdentityProviderProtocol
  provider: string
  displayName?: string
  enabled: boolean
  clientId?: string
  clientSecret?: string
  authorizationUri?: string
  tokenUri?: string
  userInfoUri?: string
  issuer?: string
  jwksUri?: string
  redirectUri?: string
  scopes?: string
  extraConfig?: string
}): Promise<void> {
  await callApi(apiPath('/providers'), 'POST', input)
}

export async function updateProvider(uuid: string, input: {
  protocol: IdentityProviderProtocol
  provider: string
  displayName?: string
  enabled: boolean
  clientId?: string
  clientSecret?: string
  authorizationUri?: string
  tokenUri?: string
  userInfoUri?: string
  issuer?: string
  jwksUri?: string
  redirectUri?: string
  scopes?: string
  extraConfig?: string
}): Promise<void> {
  await callApi(apiPath(`/providers/${uuid}`), 'PUT', input)
}

export async function setProviderEnabled(uuid: string, enabled: boolean): Promise<void> {
  await callApi(apiPath(`/providers/${uuid}/enabled/${enabled}`), 'PATCH')
}

export async function deleteProvider(uuid: string): Promise<void> {
  await callApi(apiPath(`/providers/${uuid}`), 'DELETE')
}

export async function listSessions(): Promise<SessionView[]> {
  return (await callApi<SessionView[]>(apiPath('/sessions'), 'GET')) ?? []
}

export async function deleteSession(sessionId: string): Promise<void> {
  await callApi(apiPath(`/sessions/${encodeURIComponent(sessionId)}`), 'DELETE')
}
