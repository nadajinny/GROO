<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-header__title">내 프로필</h2>
      <p class="page-header__subtitle">
        핸들을 확인하고 닉네임을 변경하면 새 태그가 발급됩니다.
      </p>
    </div>

    <section class="page-section app-card" v-if="!authStore.user">
      <div class="empty-state">프로필을 확인하려면 먼저 로그인해주세요.</div>
    </section>

    <section class="page-section app-card" v-else>
      <div class="profile-header">
        <div>
          <div class="text-muted">현재 핸들</div>
          <h3>{{ authStore.profile?.handle ?? '생성 중...' }}</h3>
        </div>
        <div class="avatar-large">
          <img v-if="authStore.user.photoUrl" :src="authStore.user.photoUrl" alt="avatar" />
          <span v-else>{{ initials }}</span>
        </div>
      </div>

      <p class="text-muted">
        닉네임을 변경하면 새 태그 5자리가 자동으로 할당됩니다. 저장 후 그룹/친구들에게 새로운 핸들을 안내해주세요.
      </p>

      <form class="profile-form" @submit.prevent="saveNickname">
        <label>닉네임</label>
        <input v-model="nickname" class="input" type="text" placeholder="예: jinsun.dev" />
        <div class="card-actions">
          <button class="btn btn-primary" type="submit" :disabled="isSaving">
            {{ isSaving ? '저장 중...' : '닉네임 저장' }}
          </button>
        </div>
      </form>

      <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>
      <div v-if="successMessage" class="success-banner">{{ successMessage }}</div>

      <div class="divider" role="presentation"></div>
      <div class="theme-section">
        <div>
          <h4>테마 설정</h4>
          <p class="text-muted">다크/라이트 모드를 선택하면 앱 전체에 즉시 반영됩니다.</p>
        </div>
        <div class="theme-options">
          <label
            v-for="option in themeOptions"
            :key="option.value"
            :class="['theme-option', { active: selectedTheme === option.value }]"
          >
            <input
              type="radio"
              :value="option.value"
              v-model="selectedTheme"
              name="theme"
            />
            <div class="theme-preview" :data-mode="option.value">
              <span class="theme-preview__sky"></span>
              <span class="theme-preview__card"></span>
            </div>
            <div>
              <strong>{{ option.label }}</strong>
              <p>{{ option.description }}</p>
            </div>
          </label>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import { useAuthStore } from '../stores/auth'
import * as userProfileService from '../services/userProfileService'
import * as userDirectory from '../services/userDirectory'
import { useThemeStore, type ThemeMode } from '../stores/theme'

const authStore = useAuthStore()
const themeStore = useThemeStore()
const nickname = ref('')
const isSaving = ref(false)
const errorMessage = ref<string | null>(null)
const successMessage = ref<string | null>(null)
const themeOptions = [
  {
    value: 'dark',
    label: '다크 모드',
    description: '집중도 높은 네온 글래스 UI'
  },
  {
    value: 'light',
    label: '라이트 모드',
    description: '밝고 명확한 화이트 UI'
  }
] as const

watch(
  () => authStore.profile,
  (profile) => {
    if (profile) {
      nickname.value = profile.displayName || profile.userId
    }
  },
  { immediate: true }
)

const initials = computed(() => {
  const name = authStore.profile?.displayName ?? authStore.user?.displayName ?? 'U'
  return name
    .split(' ')
    .map((part) => part[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()
})

const selectedTheme = computed({
  get: () => themeStore.currentTheme,
  set: (value: ThemeMode) => themeStore.setTheme(value)
})

async function saveNickname() {
  const user = authStore.user
  if (!user) return
  if (!nickname.value.trim()) {
    errorMessage.value = '닉네임을 입력해주세요.'
    return
  }
  isSaving.value = true
  errorMessage.value = null
  successMessage.value = null
  try {
    const updated = await userProfileService.updateNickname({
      uid: user.uid,
      newNickname: nickname.value.trim(),
      photoUrl: user.photoUrl
    })
    userDirectory.cacheProfile(updated)
    authStore.updateProfile(updated)
    successMessage.value = `새 핸들이 생성되었습니다: ${updated.handle}`
  } catch (error) {
    errorMessage.value = '닉네임을 저장하지 못했습니다.'
  } finally {
    isSaving.value = false
  }
}
</script>

<style scoped>
.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.avatar-large {
  width: 80px;
  height: 80px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  border: 1px solid var(--app-border);
}

.avatar-large img {
  width: 100%;
  height: 100%;
  border-radius: 24px;
  object-fit: cover;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 20px;
}

.error-banner {
  margin-top: 16px;
  border-radius: 12px;
  padding: 12px;
  background: rgba(255, 107, 107, 0.15);
  border: 1px solid rgba(255, 107, 107, 0.4);
}

.success-banner {
  margin-top: 16px;
  border-radius: 12px;
  padding: 12px;
  background: rgba(74, 222, 128, 0.15);
  border: 1px solid rgba(74, 222, 128, 0.4);
}

.divider {
  margin: 32px 0;
  height: 1px;
  background: var(--app-border);
}

.theme-section h4 {
  margin: 0 0 4px;
}

.theme-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.theme-option {
  border: 1px solid var(--app-border);
  border-radius: 16px;
  padding: 16px;
  display: flex;
  gap: 16px;
  align-items: center;
  cursor: pointer;
  transition: border 0.2s ease, background 0.2s ease;
}

.theme-option.active {
  border-color: var(--app-border-light);
  background: rgba(255, 255, 255, 0.04);
}

.theme-option strong {
  display: block;
}

.theme-option p {
  margin: 2px 0 0;
  color: var(--app-text-muted);
  font-size: 13px;
}

.theme-option input {
  display: none;
}

.theme-preview {
  width: 80px;
  height: 54px;
  border-radius: 12px;
  border: 1px solid var(--app-border);
  position: relative;
  overflow: hidden;
}

.theme-preview__sky {
  position: absolute;
  inset: 0;
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.2), transparent);
}

.theme-preview__card {
  position: absolute;
  width: 60%;
  height: 40%;
  left: 20%;
  bottom: 16%;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.theme-preview[data-mode='light'] {
  background: linear-gradient(160deg, #e3e7ff, #fff);
}

.theme-preview[data-mode='light'] .theme-preview__card {
  background: rgba(255, 255, 255, 0.8);
  border-color: rgba(34, 42, 115, 0.2);
}

.theme-preview[data-mode='dark'] {
  background: linear-gradient(160deg, #050415, #1b1c3a);
}

.theme-preview[data-mode='dark'] .theme-preview__card {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.35);
}
</style>
