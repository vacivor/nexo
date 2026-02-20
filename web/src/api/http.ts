export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'

export const SESSION_HEADER = 'X-Session-Id'
export const CSRF_HEADER = 'X-CSRF-Token'
const CSRF_STORAGE_KEY = 'nexoCsrfToken'
const SESSION_STORAGE_KEY = 'nexoSessionId'

async function ensureCsrfToken(): Promise<string | undefined> {
  const cached = localStorage.getItem(CSRF_STORAGE_KEY)
  const cachedSessionId = localStorage.getItem(SESSION_STORAGE_KEY)
  if (cached && cachedSessionId) {
    return cached
  }
  const response = await fetch('/csrf', {
    method: 'GET',
    credentials: 'include',
  })
  const responseSession = response.headers.get(SESSION_HEADER)
  if (responseSession) {
    localStorage.setItem(SESSION_STORAGE_KEY, responseSession)
  }
  const responseCsrf = response.headers.get(CSRF_HEADER)
  if (responseCsrf) {
    localStorage.setItem(CSRF_STORAGE_KEY, responseCsrf)
  }
  if (!response.ok) {
    return undefined
  }
  const payload = (await response.json()) as { token?: string }
  if (payload?.token) {
    localStorage.setItem(CSRF_STORAGE_KEY, payload.token)
    return payload.token
  }
  return undefined
}

export async function callApi<T>(path: string, method: HttpMethod, body?: unknown): Promise<T | undefined> {
  const requestBody = body === undefined ? undefined : JSON.stringify(body)
  const createHeaders = async (): Promise<Record<string, string>> => {
    const headers: Record<string, string> = {}
    if (method !== 'GET') {
      const csrfToken = await ensureCsrfToken()
      if (csrfToken) {
        headers[CSRF_HEADER] = csrfToken
      }
    }
    const sessionId = localStorage.getItem(SESSION_STORAGE_KEY)
    if (sessionId) {
      headers[SESSION_HEADER] = sessionId
    }
    if (body !== undefined) {
      headers['Content-Type'] = 'application/json'
    }
    return headers
  }
  const updateFromResponseHeaders = (response: Response): void => {
    const responseSession = response.headers.get(SESSION_HEADER)
    if (responseSession) {
      localStorage.setItem(SESSION_STORAGE_KEY, responseSession)
    }
    const responseCsrf = response.headers.get(CSRF_HEADER)
    if (responseCsrf) {
      localStorage.setItem(CSRF_STORAGE_KEY, responseCsrf)
    }
  }
  let response = await fetch(path, {
    method,
    headers: await createHeaders(),
    credentials: 'include',
    body: requestBody,
  })
  updateFromResponseHeaders(response)
  if (!response.ok && method !== 'GET' && response.status === 403) {
    const errorPayload = (await response.clone().json().catch(() => undefined)) as { error?: string } | undefined
    if (errorPayload?.error === 'invalid_csrf_token') {
      localStorage.removeItem(CSRF_STORAGE_KEY)
      await ensureCsrfToken()
      response = await fetch(path, {
        method,
        headers: await createHeaders(),
        credentials: 'include',
        body: requestBody,
      })
      updateFromResponseHeaders(response)
    }
  }
  if (!response.ok) {
    if (response.status === 403) {
      localStorage.removeItem(CSRF_STORAGE_KEY)
      localStorage.removeItem(SESSION_STORAGE_KEY)
    }
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
