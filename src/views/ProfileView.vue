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
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import { useAuthStore } from '../stores/auth'
import * as userProfileService from '../services/userProfileService'
import * as userDirectory from '../services/userDirectory'

const authStore = useAuthStore()
const nickname = ref('')
const isSaving = ref(false)
const errorMessage = ref<string | null>(null)
const successMessage = ref<string | null>(null)

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
</style>
