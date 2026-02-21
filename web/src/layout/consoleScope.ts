export type ConsoleScope = 'platform' | 'admin'

export function resolveConsoleScope(pathname?: string): ConsoleScope {
  const value = pathname ?? (typeof window !== 'undefined' ? window.location.pathname : '')
  if (value.startsWith('/platform')) {
    return 'platform'
  }
  return 'admin'
}

export function resolveConsoleBasePath(pathname?: string): string {
  const scope = resolveConsoleScope(pathname)
  if (scope === 'platform') {
    return '/platform'
  }
  return '/admin'
}

export function resolveConsoleApiBase(pathname?: string): string {
  const scope = resolveConsoleScope(pathname)
  if (scope === 'platform') {
    return '/api/platform'
  }
  return '/api/admin'
}
