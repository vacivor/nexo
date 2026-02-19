export type ConsoleScope = 'platform' | 'tenant' | 'admin'

export function resolveConsoleScope(pathname?: string): ConsoleScope {
  const value = pathname ?? (typeof window !== 'undefined' ? window.location.pathname : '')
  if (value.startsWith('/platform')) {
    return 'platform'
  }
  if (value.startsWith('/tenant')) {
    return 'tenant'
  }
  return 'admin'
}

export function resolveConsoleBasePath(pathname?: string): string {
  const scope = resolveConsoleScope(pathname)
  if (scope === 'platform') {
    return '/platform'
  }
  if (scope === 'tenant') {
    return '/tenant'
  }
  return '/admin'
}

export function resolveConsoleApiBase(pathname?: string): string {
  const scope = resolveConsoleScope(pathname)
  if (scope === 'platform') {
    return '/api/platform'
  }
  if (scope === 'tenant') {
    return '/api/tenant'
  }
  return '/api/admin'
}
