import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export type ThemeMode = 'dark' | 'light'

const STORAGE_KEY = 'groo-theme'

function applyTheme(theme: ThemeMode) {
  if (typeof document === 'undefined') return
  document.documentElement.dataset.theme = theme
}

export const useThemeStore = defineStore('theme', () => {
  const mode = ref<ThemeMode>('dark')

  function init() {
    if (typeof window !== 'undefined') {
      const stored = window.localStorage.getItem(STORAGE_KEY) as ThemeMode | null
      if (stored === 'dark' || stored === 'light') {
        mode.value = stored
      }
    }
    applyTheme(mode.value)
  }

  function setTheme(next: ThemeMode) {
    mode.value = next
    if (typeof window !== 'undefined') {
      window.localStorage.setItem(STORAGE_KEY, next)
    }
    applyTheme(next)
  }

  function toggleTheme() {
    setTheme(mode.value === 'dark' ? 'light' : 'dark')
  }

  return {
    currentTheme: computed(() => mode.value),
    init,
    setTheme,
    toggleTheme
  }
})
