<template>
  <div class="app-layout">
    <div class="app-layout__backdrop" aria-hidden="true">
      <span class="orb orb--one"></span>
      <span class="orb orb--two"></span>
    </div>
    <header class="app-layout__header">
      <RouterLink class="app-layout__brand" :to="brandDestination">
        <div class="logo-mark">
          <span>G</span>
        </div>
        <div>
          <p class="eyebrow">Collaboration Platform</p>
          <div class="logo-title">GROO</div>
        </div>
      </RouterLink>
      <nav class="app-layout__nav">
        <RouterLink
          v-for="item in destinations"
          :key="item.path"
          :to="item.path"
          class="app-layout__nav-link"
          :class="{ active: route.path.startsWith(item.path) }"
        >
          <span class="nav-label">{{ item.label }}</span>
        </RouterLink>
      </nav>
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

const brandDestination = computed(() =>
  authStore.user ? '/app/dashboard' : '/'
)
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.app-layout__backdrop {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  opacity: 0.35;
}

.orb {
  position: absolute;
  width: 480px;
  height: 480px;
  border-radius: 50%;
  filter: blur(160px);
}

.orb--one {
  top: -120px;
  left: -120px;
  background: var(--app-orb-1);
}

.orb--two {
  bottom: -180px;
  right: -140px;
  background: var(--app-orb-2);
}

.app-layout__header {
  position: sticky;
  top: 0;
  background: var(--app-header-bg);
  border-bottom: 1px solid var(--app-header-border);
  padding: 16px clamp(16px, 4vw, 40px);
  display: flex;
  align-items: center;
  gap: 20px;
  z-index: 5;
  backdrop-filter: blur(30px);
}

.app-layout__brand {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  text-decoration: none;
  color: inherit;
}

.logo-mark {
  width: 56px;
  height: 56px;
  border-radius: 18px;
  background: linear-gradient(135deg, #8e6bff, #5db7ff);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 24px;
  color: #050412;
  box-shadow: 0 15px 35px rgba(111, 127, 255, 0.5);
}

.eyebrow {
  font-size: 11px;
  letter-spacing: 0.22em;
  margin: 0;
  color: var(--app-text-muted);
}

.logo-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.app-layout__nav {
  display: flex;
  gap: 6px;
  flex: 1;
  justify-content: flex-start;
}

.app-layout__nav-link {
  display: inline-flex;
  align-items: center;
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
  background: rgba(255, 255, 255, 0.08);
}

.app-layout__user {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-left: auto;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.25);
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
  gap: 20px;
  padding: 24px clamp(16px, 4vw, 40px) 80px;
}

.app-layout__sidebar {
  width: 290px;
  border-right: none;
  position: sticky;
  top: 120px;
  align-self: flex-start;
}

.app-layout__content {
  flex: 1;
  min-height: 100%;
}

@media (max-width: 1024px) {
  .app-layout__header {
    flex-direction: column;
  }

  .app-layout__body {
    flex-direction: column;
  }

  .app-layout__sidebar {
    width: 100%;
    position: static;
  }
}
</style>
