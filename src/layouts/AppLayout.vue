<template>
  <div class="app-layout">
    <header class="app-layout__header">
      <div class="app-layout__brand">
        <div class="logo">Lab Tool</div>
        <nav class="app-layout__nav">
        <RouterLink
          v-for="item in destinations"
          :key="item.path"
          :to="item.path"
          class="app-layout__nav-link"
          :class="{ active: route.path.startsWith(item.path) }"
        >
          <span>{{ item.label }}</span>
        </RouterLink>
        </nav>
      </div>
      <div class="app-layout__user" v-if="authStore.user">
        <div class="user-meta">
          <img
            v-if="authStore.user?.photoUrl"
            :src="authStore.user.photoUrl"
            alt="avatar"
            class="user-avatar"
          />
          <div v-else class="user-avatar fallback">
            <span>{{ initials }}</span>
          </div>
          <div>
            <div class="user-name">{{ profileName }}</div>
            <div class="user-handle">
              핸들 {{ authStore.profile?.handle ?? '확인 중...' }}
            </div>
          </div>
        </div>
        <RouterLink class="btn btn-muted" to="/app/profile">
          프로필
        </RouterLink>
        <button class="btn btn-outline" :disabled="authStore.isLoading" @click="authStore.signOut">
          {{ authStore.isLoading ? '로그아웃 중...' : '로그아웃' }}
        </button>
      </div>
    </header>
    <div class="app-layout__body">
      <aside class="app-layout__sidebar" v-if="authStore.user">
        <GroupSidebar />
      </aside>
      <main class="app-layout__content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import GroupSidebar from '../components/groups/GroupSidebar.vue'

const route = useRoute()
const authStore = useAuthStore()

const destinations = [
  { path: '/app/dashboard', label: '대시보드' },
  { path: '/app/groups', label: '그룹' },
  { path: '/app/projects', label: '프로젝트' },
  { path: '/app/messages', label: '메시지' }
]

const profileName = computed(
  () => authStore.profile?.displayName ?? authStore.user?.displayName ?? '이름 없음'
)

const initials = computed(() => {
  const name = profileName.value
  if (!name) return 'U'
  return name
    .split(' ')
    .map((part) => part[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()
})
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-layout__header {
  position: sticky;
  top: 0;
  background: rgba(5, 5, 5, 0.9);
  border-bottom: 1px solid var(--app-border);
  padding: 20px clamp(16px, 4vw, 40px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  z-index: 10;
  backdrop-filter: blur(24px);
}

.app-layout__brand {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.logo {
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.app-layout__nav {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.app-layout__nav-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 999px;
  text-decoration: none;
  color: var(--app-text-muted);
  border: 1px solid transparent;
  transition: border 0.2s ease, color 0.2s ease, background 0.2s ease;
}

.app-layout__nav-link.active {
  border-color: var(--app-border-light);
  color: var(--app-text);
  background: rgba(255, 255, 255, 0.05);
}

.app-layout__user {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.user-avatar.fallback {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid var(--app-border);
}

.user-name {
  font-weight: 600;
}

.user-handle {
  color: var(--app-text-muted);
  font-size: 13px;
}

.app-layout__body {
  flex: 1;
  display: flex;
}

.app-layout__sidebar {
  width: 270px;
  border-right: 1px solid var(--app-border);
  background: rgba(0, 0, 0, 0.2);
  padding: 20px;
}

.app-layout__content {
  flex: 1;
  min-height: 100%;
  background: transparent;
}

@media (max-width: 1024px) {
  .app-layout__body {
    flex-direction: column;
  }
  .app-layout__sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid var(--app-border);
  }
}
</style>
