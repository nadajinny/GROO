import { defineStore } from 'pinia'
import { computed, ref, watch } from 'vue'
import { GoogleAuthProvider, signInWithPopup, signOut, onAuthStateChanged } from 'firebase/auth'
import type { User } from 'firebase/auth'

import { auth } from '../firebase'
import type { AppUser, UserProfile } from '../types'
import * as userDirectory from '../services/userDirectory'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<AppUser | null>(null)
  const profile = ref<UserProfile | null>(null)
  const isLoading = ref(false)
  const errorMessage = ref<string | null>(null)
  const initialized = ref(false)

  let unsubscribeAuth: (() => void) | null = null

  function init() {
    if (unsubscribeAuth) return
    unsubscribeAuth = onAuthStateChanged(auth, async (firebaseUser) => {
      if (firebaseUser) {
        user.value = mapUser(firebaseUser)
        try {
          const resolved = await userDirectory.ensureForUser(user.value)
          profile.value = resolved
        } catch (error) {
          console.error('Failed to sync profile', error)
        }
      } else {
        user.value = null
        profile.value = null
      }
      initialized.value = true
    })
  }

  async function signInWithGoogle() {
    if (isLoading.value) return
    isLoading.value = true
    errorMessage.value = null
    try {
      const provider = new GoogleAuthProvider()
      provider.setCustomParameters({ prompt: 'select_account' })
      await signInWithPopup(auth, provider)
    } catch (error: any) {
      console.error('Sign-in failed', error)
      errorMessage.value =
        error?.message ?? 'Google 로그인을 진행할 수 없습니다. 잠시 후 다시 시도해주세요.'
    } finally {
      isLoading.value = false
    }
  }

  async function signOutFromApp() {
    if (isLoading.value) return
    isLoading.value = true
    errorMessage.value = null
    try {
      await signOut(auth)
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

  function mapUser(firebaseUser: User): AppUser {
    return {
      uid: firebaseUser.uid,
      displayName: firebaseUser.displayName,
      photoUrl: firebaseUser.photoURL
    }
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

  return {
    user,
    profile,
    isLoading,
    errorMessage,
    initialized,
    init,
    signInWithGoogle,
    signOut: signOutFromApp,
    updateProfile,
    waitForReady
  }
})
