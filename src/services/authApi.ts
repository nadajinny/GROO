import { apiRequest, clearTokens, getRefreshToken, setTokens } from './apiClient'

export interface AuthPayload {
  email: string
  password: string
}

export interface RegisterPayload extends AuthPayload {
  displayName: string
}

export interface AuthTokens {
  accessToken: string
  refreshToken: string
  expiresInSeconds: number
}

export interface BackendUser {
  id: number
  email: string
  displayName: string
  role: string
  status: string
  createdAt?: string
}

export async function register(payload: RegisterPayload) {
  const tokens = await apiRequest<AuthTokens>('/api/auth/register', {
    method: 'POST',
    body: payload,
    skipAuth: true
  })
  setTokens(tokens.accessToken, tokens.refreshToken)
  return tokens
}

export async function login(payload: AuthPayload) {
  const tokens = await apiRequest<AuthTokens>('/api/auth/login', {
    method: 'POST',
    body: payload,
    skipAuth: true
  })
  setTokens(tokens.accessToken, tokens.refreshToken)
  return tokens
}

export async function logout() {
  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    clearTokens()
    return
  }
  try {
    await apiRequest<void>('/api/auth/logout', {
      method: 'POST',
      body: { refreshToken }
    })
  } catch {
    // ignore failure
  } finally {
    clearTokens()
  }
}

export async function fetchCurrentUser() {
  return apiRequest<BackendUser>('/api/users/me')
}
