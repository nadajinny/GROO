import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import router from '../router'

import type { AppUser, UserProfile } from '../types'
import * as userDirectory from '../services/userDirectory'
import { getAccessToken } from '../services/apiClient'
import * as authApi from '../services/authApi'
import type { BackendUser } from '../services/authApi'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<AppUser | null>(null)
  const profile = ref<UserProfile | null>(null)
  const backendUser = ref<BackendUser | null>(null)
  const isLoading = ref(false)
  const errorMessage = ref<string | null>(null)
  const initialized = ref(false)

  async function init() {
    if (initialized.value) return
    if (!getAccessToken()) {
      initialized.value = true
      return
    }
    await restoreSession()
    initialized.value = true
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
      await authApi.register({
        email: params.email,
        password: params.password,
        displayName: sanitizeUserId(params.userId)
      })
      await restoreSession()
    } catch (error: any) {
      console.error('Email sign-up failed', error)
      errorMessage.value =
        error?.message ?? '회원가입을 진행하지 못했습니다. 잠시 후 다시 시도해주세요.'
    } finally {
      isLoading.value = false
    }
  }

  async function signInWithEmail(params: { email: string; password: string }) {
    if (isLoading.value) return
    isLoading.value = true
    errorMessage.value = null
    try {
      await authApi.login({
        email: params.email,
        password: params.password
      })
      await restoreSession()
    } catch (error: any) {
      console.error('Email sign-in failed', error)
      errorMessage.value = error?.message ?? '로그인을 진행하지 못했습니다. 잠시 후 다시 시도해주세요.'
    } finally {
      isLoading.value = false
    }
  }

  async function signOutFromApp() {
    if (isLoading.value) return
    isLoading.value = true
    errorMessage.value = null
    try {
      await authApi.logout()
      user.value = null
      profile.value = null
      backendUser.value = null
      if (router.currentRoute.value.path !== '/') {
        router.push('/')
      }
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

  async function restoreSession() {
    try {
      const me = await authApi.fetchCurrentUser()
      applyBackendUser(me)
    } catch (error) {
      console.error('Failed to restore session', error)
      await authApi.logout()
      user.value = null
      profile.value = null
      backendUser.value = null
    }
  }

  function applyBackendUser(me: BackendUser) {
    backendUser.value = me
    const uid = me.id.toString()
    const email = me.email ?? ''
    const fallback = email ? email.split('@')[0] : 'User'
    const displayName = ((me.displayName ?? '').trim() || fallback) as string
    user.value = {
      uid,
      displayName,
      photoUrl: null
    }
    const resolved: UserProfile = {
      uid,
      userId: displayName,
      tag: email,
      displayName,
      handle: email,
      photoUrl: null
    }
    profile.value = resolved
    userDirectory.cacheProfile(resolved)
  }

  return {
    user,
    profile,
    backendUser,
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

function sanitizeUserId(userId: string) {
  const trimmed = userId.trim()
  if (!trimmed) return 'User'
  return trimmed.replace(/\s+/g, '').slice(0, 30)
}
