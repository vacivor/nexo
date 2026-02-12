export type LoginResponse = {
  principal: string
  authorities: string[]
}

export type User = {
  id: number
  username: string
  email?: string
  phone?: string
}

export type Tenant = {
  id: number
  uuid: string
  name: string
}

export type Application = {
  id: number
  uuid: string
  clientId: string
  clientSecret: string
  name: string
  redirectUris: string[]
}

export type IdentityProviderProtocol = 'OAUTH2' | 'OIDC' | 'SOCIAL'

export type IdentityProvider = {
  id: number
  uuid: string
  protocol: IdentityProviderProtocol
  provider: string
  displayName?: string
  enabled: boolean
  clientId?: string
  authorizationUri?: string
  tokenUri?: string
  userInfoUri?: string
  jwksUri?: string
  issuer?: string
  redirectUri?: string
  scopes?: string
  extraConfig?: string
}
