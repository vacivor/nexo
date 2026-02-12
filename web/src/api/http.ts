export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'

export const SESSION_HEADER = 'X-Session-Id'

export async function callApi<T>(path: string, method: HttpMethod, body?: unknown): Promise<T | undefined> {
  const headers: Record<string, string> = {}
  const sessionId = localStorage.getItem('nexoSessionId')
  if (sessionId) {
    headers[SESSION_HEADER] = sessionId
  }
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }
  const response = await fetch(path, {
    method,
    headers,
    credentials: 'include',
    body: body === undefined ? undefined : JSON.stringify(body),
  })
  const responseSession = response.headers.get(SESSION_HEADER)
  if (responseSession) {
    localStorage.setItem('nexoSessionId', responseSession)
  }
  if (!response.ok) {
    let detail = `HTTP ${response.status}`
    try {
      const err = (await response.json()) as { error?: string }
      if (err?.error) {
        detail = `${detail} ${err.error}`
      }
    } catch {
      // ignore non-json error body
    }
    throw new Error(detail)
  }
  if (response.status === 204) {
    return undefined
  }
  return (await response.json()) as T
}
