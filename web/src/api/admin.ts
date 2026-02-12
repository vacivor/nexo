import { callApi } from './http'
import type { Application, IdentityProvider, IdentityProviderProtocol, LoginResponse, Tenant, User } from './types'

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
  return (await callApi<User[]>('/api/admin/users', 'GET')) ?? []
}

export async function createUser(input: {
  username: string
  email?: string
  phone?: string
  password: string
}): Promise<void> {
  await callApi('/api/admin/users', 'POST', input)
}

export async function updateUser(userId: string, input: {
  username: string
  email?: string
  phone?: string
}): Promise<void> {
  await callApi(`/api/admin/users/${userId}`, 'PUT', input)
}

export async function resetUserPassword(userId: string, password: string): Promise<void> {
  await callApi(`/api/admin/users/${userId}/password`, 'PATCH', { password })
}

export async function deleteUser(userId: number): Promise<void> {
  await callApi(`/api/admin/users/${userId}`, 'DELETE')
}

export async function listTenants(): Promise<Tenant[]> {
  return (await callApi<Tenant[]>('/api/admin/tenants', 'GET')) ?? []
}

export async function createTenant(name: string): Promise<void> {
  await callApi('/api/admin/tenants', 'POST', { name })
}

export async function createApplication(input: {
  tenantId: string
  name: string
  redirectUris: string[]
}): Promise<Application | undefined> {
  return callApi<Application>('/api/admin/applications', 'POST', input)
}

export async function listProviders(): Promise<IdentityProvider[]> {
  return (await callApi<IdentityProvider[]>('/api/admin/providers', 'GET')) ?? []
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
  await callApi('/api/admin/providers', 'POST', input)
}

export async function setProviderEnabled(uuid: string, enabled: boolean): Promise<void> {
  await callApi(`/api/admin/providers/${uuid}/enabled/${enabled}`, 'PATCH')
}

export async function deleteProvider(uuid: string): Promise<void> {
  await callApi(`/api/admin/providers/${uuid}`, 'DELETE')
}
