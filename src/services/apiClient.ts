const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
const ACCESS_TOKEN_KEY = 'groo_access_token'
const REFRESH_TOKEN_KEY = 'groo_refresh_token'

let cachedAccessToken: string | null = getStoredToken(ACCESS_TOKEN_KEY)
let cachedRefreshToken: string | null = getStoredToken(REFRESH_TOKEN_KEY)
let refreshPromise: Promise<string | null> | null = null

type RequestOptions = Omit<RequestInit, 'body'> & {
  body?: any
  skipAuth?: boolean
}

interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string | null
  code?: string | null
}

export function getAccessToken() {
  return cachedAccessToken
}

export function getRefreshToken() {
  return cachedRefreshToken
}

export function setTokens(accessToken: string, refreshToken: string) {
  cachedAccessToken = accessToken
  cachedRefreshToken = refreshToken
  setStoredToken(ACCESS_TOKEN_KEY, accessToken)
  setStoredToken(REFRESH_TOKEN_KEY, refreshToken)
}

export function clearTokens() {
  cachedAccessToken = null
  cachedRefreshToken = null
  removeStoredToken(ACCESS_TOKEN_KEY)
  removeStoredToken(REFRESH_TOKEN_KEY)
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { skipAuth, body, ...init } = options
  const headers = new Headers(options.headers ?? {})
  headers.set('Accept', 'application/json')

  let requestBody = body

  if (requestBody && typeof requestBody === 'object' && !(requestBody instanceof FormData)) {
    headers.set('Content-Type', 'application/json')
    requestBody = JSON.stringify(requestBody)
  }

  if (!skipAuth && cachedAccessToken) {
    headers.set('Authorization', `Bearer ${cachedAccessToken}`)
  }

  const response = await fetch(buildApiUrl(path), {
    ...init,
    headers,
    body: requestBody
  })

  if (response.status === 401 && !skipAuth && cachedRefreshToken) {
    const newToken = await refreshAccessToken()
    if (newToken) {
      return apiRequest(path, options)
    }
    throw new Error('로그인이 만료되었습니다.')
  }

  return handleResponse<T>(response)
}

async function handleResponse<T>(response: Response): Promise<T> {
  let payload: ApiResponse<T> | null = null
  try {
    payload = (await response.json()) as ApiResponse<T>
  } catch {
    // ignore
  }

  if (!response.ok || !payload?.success) {
    const message = payload?.message ?? `요청이 실패했습니다. (${response.status})`
    const error = new Error(message)
    ;(error as any).code = payload?.code ?? response.status
    throw error
  }

  return payload.data
}

async function refreshAccessToken() {
  if (!cachedRefreshToken) {
    clearTokens()
    return null
  }

  if (!refreshPromise) {
    refreshPromise = (async () => {
      try {
        const response = await fetch(buildApiUrl('/api/auth/refresh'), {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json'
          },
          body: JSON.stringify({ refreshToken: cachedRefreshToken }),
          // refresh uses refresh token, no access token header needed
          credentials: 'include'
        })
        const payload = (await response.json()) as ApiResponse<{
          accessToken: string
          refreshToken: string
        }>
        if (!response.ok || !payload?.success) {
          clearTokens()
          return null
        }
        setTokens(payload.data.accessToken, payload.data.refreshToken)
        return payload.data.accessToken
      } catch {
        clearTokens()
        return null
      } finally {
        refreshPromise = null
      }
    })()
  }
  return refreshPromise
}

function buildApiUrl(path: string) {
  const baseUrl = String(API_BASE_URL ?? '').replace(/\/+$/, '')
  const normalizedPath = path.startsWith('/') ? path : `/${path}`

  if (!baseUrl) return normalizedPath

  if (
    baseUrl.toLowerCase().endsWith('/api') &&
    (normalizedPath === '/api' || normalizedPath.startsWith('/api/'))
  ) {
    return `${baseUrl}${normalizedPath.slice('/api'.length)}`
  }

  return `${baseUrl}${normalizedPath}`
}

function getStoredToken(key: string) {
  if (typeof window === 'undefined') return null
  return window.localStorage.getItem(key)
}

function setStoredToken(key: string, value: string) {
  if (typeof window === 'undefined') return
  window.localStorage.setItem(key, value)
}

function removeStoredToken(key: string) {
  if (typeof window === 'undefined') return
  window.localStorage.removeItem(key)
}
