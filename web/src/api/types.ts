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

export type Application = {
  id: number
  uuid: string
  clientType?: string
  clientId: string
  clientSecret: string
  name: string
  description?: string
  logo?: string
  idTokenExpiration?: number
  refreshTokenExpiration?: number
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

export type SessionView = {
  id: string
  principal?: string
  createdAt: string
  lastAccessedAt: string
  expiresAt?: string
  isNew: boolean
}

export type SessionPage = {
  items: SessionView[]
  nextCursor?: string
  hasMore: boolean
}

export type ConsentView = {
  requestId: string
  csrfToken: string
  clientId: string
  scopes: string[]
}

export type ConsentDecision = {
  redirectUri: string
}

export type UserConsentGrant = {
  clientId: string
  clientName: string
  clientLogo?: string
  scopes: string[]
  grantedAt?: string
}
