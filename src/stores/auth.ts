import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

import type { AppUser, UserProfile } from '../types'
import * as userDirectory from '../services/userDirectory'
import * as accountService from '../services/accountService'
import type { AccountRecord } from '../services/accountService'

const SESSION_KEY = 'groo-account-uid'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<AppUser | null>(null)
  const profile = ref<UserProfile | null>(null)
  const isLoading = ref(false)
  const errorMessage = ref<string | null>(null)
  const initialized = ref(false)
  let isRestoringSession = false

  function init() {
    if (initialized.value || isRestoringSession) return
    isRestoringSession = true
    const savedUid = getPersistedUid()
    if (!savedUid) {
      initialized.value = true
      isRestoringSession = false
      return
    }
    restoreSession(savedUid)
      .catch((error) => {
        console.error('Failed to restore session', error)
        clearPersistedUid()
      })
      .finally(() => {
        initialized.value = true
        isRestoringSession = false
      })
  }

  async function registerWithEmail(params: {
    email: string
    password: string
    userId: string
  }) {
    if (isLoading.value) return
    isLoading.value = true
    errorMessage.value = null
    try {
      const existing = await accountService.findByEmail(params.email)
      if (existing) {
        throw new Error('이미 사용 중인 이메일입니다.')
      }
      const uid = generateUid()
      const passwordHash = await hashPassword(params.password)
      const sanitizedUserId = sanitizeUserId(params.userId)
      await accountService.createAccount({
        uid,
        email: params.email,
        passwordHash,
        userId: sanitizedUserId
      })
      await establishSessionFromAccount({
        uid,
        email: params.email,
        passwordHash,
        userId: sanitizedUserId
      })
    } catch (error: any) {
      console.error('Email sign-up failed', error)
      errorMessage.value =
        error?.message ?? '회원가입을 진행할 수 없습니다. 잠시 후 다시 시도해주세요.'
    } finally {
      isLoading.value = false
    }
  }

  async function signInWithEmail(params: { email: string; password: string }) {
    if (isLoading.value) return
    isLoading.value = true
    errorMessage.value = null
    try {
      const account = await accountService.findByEmail(params.email)
      if (!account) {
        throw new Error('등록되지 않은 계정입니다.')
      }
      const passwordHash = await hashPassword(params.password)
      if (account.passwordHash !== passwordHash) {
        throw new Error('비밀번호가 올바르지 않습니다.')
      }
      await establishSessionFromAccount(account)
    } catch (error: any) {
      console.error('Email sign-in failed', error)
      errorMessage.value =
        error?.message ?? '로그인을 진행할 수 없습니다. 잠시 후 다시 시도해주세요.'
    } finally {
      isLoading.value = false
    }
  }

  async function signOutFromApp() {
    if (isLoading.value) return
    isLoading.value = true
    errorMessage.value = null
    try {
      clearPersistedUid()
      user.value = null
      profile.value = null
    } catch (error: any) {
      console.error('Logout failed', error)
      errorMessage.value = error?.message ?? '로그아웃에 실패했습니다.'
    } finally {
      isLoading.value = false
    }
  }

  function updateProfile(next: UserProfile) {
    profile.value = next
    userDirectory.cacheProfile(next)
  }

  function waitForReady() {
    if (initialized.value) return Promise.resolve(true)
    return new Promise((resolve) => {
      const stop = watch(
        () => initialized.value,
        (value) => {
          if (value) {
            stop()
            resolve(true)
          }
        }
      )
    })
  }

  function clearError() {
    errorMessage.value = null
  }

  async function restoreSession(uid: string) {
    const account = await accountService.findByUid(uid)
    if (!account) throw new Error('계정을 찾을 수 없습니다.')
    await establishSessionFromAccount(account)
  }

  async function establishSessionFromAccount(account: AccountRecord) {
    const appUser = mapAccountToUser(account)
    user.value = appUser
    try {
      const resolved = await userDirectory.ensureForUser(appUser)
      profile.value = resolved
      persistUid(appUser.uid)
    } catch (error) {
      console.error('Failed to sync profile', error)
      throw error
    } finally {
      initialized.value = true
    }
  }

  return {
    user,
    profile,
    isLoading,
    errorMessage,
    initialized,
    init,
    registerWithEmail,
    signInWithEmail,
    signOut: signOutFromApp,
    updateProfile,
    waitForReady,
    clearError
  }
})

function mapAccountToUser(account: AccountRecord): AppUser {
  return {
    uid: account.uid,
    displayName: account.userId,
    photoUrl: null
  }
}

function persistUid(uid: string) {
  if (typeof window === 'undefined') return
  window.localStorage.setItem(SESSION_KEY, uid)
}

function getPersistedUid(): string | null {
  if (typeof window === 'undefined') return null
  return window.localStorage.getItem(SESSION_KEY)
}

function clearPersistedUid() {
  if (typeof window === 'undefined') return
  window.localStorage.removeItem(SESSION_KEY)
}

function generateUid() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return `uid_${Math.random().toString(36).slice(2, 10)}`
}

function sanitizeUserId(userId: string) {
  const trimmed = userId.trim()
  if (!trimmed) return 'user'
  return trimmed.replace(/\s+/g, '').slice(0, 30)
}

async function hashPassword(value: string): Promise<string> {
  if (typeof crypto !== 'undefined' && crypto.subtle) {
    const encoder = new TextEncoder()
    const buffer = await crypto.subtle.digest('SHA-256', encoder.encode(value))
    return Array.from(new Uint8Array(buffer))
      .map((byte) => byte.toString(16).padStart(2, '0'))
      .join('')
  }
  return btoa(value)
}
