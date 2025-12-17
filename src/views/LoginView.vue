<template>
  <div class="login">
    <div class="login__background" aria-hidden="true"></div>
    <section class="login__hero">
      <span class="pill">Lab Tool Web</span>
      <h1>GROO로 실험실 협업을 한 번에</h1>
      <p>
        이메일 계정만으로 프로젝트, 태스크, 그룹, 메신저까지 모두 관리할 수 있도록
        Lab Tool에서 제공하던 기능을 그대로 옮겨왔습니다.
      </p>
      <div class="login__highlights">
        <article v-for="item in highlights" :key="item.title">
          <div class="login__highlights-icon">{{ item.icon }}</div>
          <div>
            <h4>{{ item.title }}</h4>
            <p>{{ item.description }}</p>
          </div>
        </article>
      </div>
    </section>
    <section class="login__card app-card">
      <div class="login__card-header">
        <p class="pill small">시작하기</p>
        <h2>GROO 계정을 만들고 로그인하세요</h2>
        <p class="text-muted">
          Firebase Authentication · Cloud Firestore 환경을 그대로 사용합니다.
        </p>
      </div>

      <div class="login__tabs">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          class="login__tab"
          :class="{ active: activeTab === tab.value }"
          type="button"
          @click="switchTab(tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="login__status" v-if="localError">
        {{ localError }}
      </div>
      <div class="login__status" v-else-if="authStore.errorMessage">
        {{ authStore.errorMessage }}
      </div>

      <form v-if="activeTab === 'login'" class="login__form" @submit.prevent="handleLogin">
        <label>이메일</label>
        <input
          v-model="loginForm.email"
          class="input"
          type="email"
          autocomplete="email"
          placeholder="you@example.com"
        />
        <label>비밀번호</label>
        <input
          v-model="loginForm.password"
          class="input"
          type="password"
          autocomplete="current-password"
          placeholder="••••••••"
        />
        <button class="btn btn-primary" type="submit" :disabled="authStore.isLoading">
          {{ authStore.isLoading ? '로그인 중...' : '로그인' }}
        </button>
      </form>

      <form v-else class="login__form" @submit.prevent="handleSignup">
        <label>아이디</label>
        <input
          v-model="signupForm.userId"
          class="input"
          type="text"
          autocomplete="nickname"
          placeholder="예: jinsunlab"
        />
        <label>이메일</label>
        <input
          v-model="signupForm.email"
          class="input"
          type="email"
          autocomplete="email"
          placeholder="you@example.com"
        />
        <label>비밀번호</label>
        <input
          v-model="signupForm.password"
          class="input"
          type="password"
          autocomplete="new-password"
          placeholder="최소 6자 이상"
        />
        <label>비밀번호 확인</label>
        <input
          v-model="signupForm.confirmPassword"
          class="input"
          type="password"
          autocomplete="new-password"
          placeholder="비밀번호를 다시 입력하세요"
        />
        <button class="btn btn-primary" type="submit" :disabled="authStore.isLoading">
          {{ authStore.isLoading ? '회원가입 중...' : '회원가입' }}
        </button>
      </form>

      <p class="login__note">
        계정을 등록하면 Lab Tool 데이터와 동기화되어 그룹·프로젝트·메시지를 그대로 사용할 수 있습니다.
      </p>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const router = useRouter()

const tabs = [
  { value: 'login', label: '로그인' },
  { value: 'signup', label: '회원가입' }
] as const

const activeTab = ref<(typeof tabs)[number]['value']>('login')
const localError = ref<string | null>(null)
const loginForm = reactive({
  email: '',
  password: ''
})
const signupForm = reactive({
  userId: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const highlights = [
  {
    title: '대시보드 요약',
    description: '마감 일정과 알림을 한눈에 조회',
    icon: '01'
  },
  {
    title: '그룹 권한 관리',
    description: '멤버 역할과 권한을 실시간 설정',
    icon: '02'
  },
  {
    title: '태스크 추적',
    description: '세부 태스크와 댓글 기록까지 제공',
    icon: '03'
  }
]

watch(
  () => authStore.user,
  (currentUser) => {
    if (currentUser) {
      router.push('/app/dashboard')
    }
  }
)

function switchTab(tab: (typeof tabs)[number]['value']) {
  if (activeTab.value === tab) return
  activeTab.value = tab
  localError.value = null
  authStore.clearError()
}

async function handleLogin() {
  if (!loginForm.email || !loginForm.password) {
    localError.value = '이메일과 비밀번호를 모두 입력해주세요.'
    return
  }
  localError.value = null
  authStore.clearError()
  await authStore.signInWithEmail({
    email: loginForm.email,
    password: loginForm.password
  })
}

async function handleSignup() {
  if (!signupForm.email || !signupForm.password) {
    localError.value = '이메일과 비밀번호를 입력해주세요.'
    return
  }
  if (!signupForm.userId.trim()) {
    localError.value = '아이디를 입력해주세요.'
    return
  }
  if (signupForm.password.length < 6) {
    localError.value = '비밀번호는 최소 6자 이상이어야 합니다.'
    return
  }
  if (signupForm.password !== signupForm.confirmPassword) {
    localError.value = '비밀번호가 일치하지 않습니다.'
    return
  }
  localError.value = null
  authStore.clearError()
  await authStore.registerWithEmail({
    email: signupForm.email,
    password: signupForm.password,
    userId: signupForm.userId.trim()
  })
}
</script>

<style scoped>
.login {
  min-height: 100vh;
  padding: clamp(32px, 6vw, 80px) clamp(20px, 6vw, 80px);
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 36px;
  align-items: center;
  position: relative;
}

.login__background {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 20% 20%, rgba(132, 118, 255, 0.4), transparent 60%),
    radial-gradient(circle at 80% 0%, rgba(76, 165, 255, 0.2), transparent 55%);
  opacity: 0.7;
  pointer-events: none;
}

.login__hero {
  position: relative;
}

.login__hero h1 {
  font-size: clamp(36px, 6vw, 56px);
  margin: 18px 0;
  line-height: 1.1;
  letter-spacing: -0.01em;
}

.login__hero p {
  color: var(--app-text-muted);
  max-width: 560px;
}

.login__highlights {
  margin-top: 32px;
  display: grid;
  gap: 14px;
}

.login__highlights article {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 16px;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.02);
  backdrop-filter: blur(10px);
}

.login__highlights-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: var(--app-primary);
}

.login__highlights h4 {
  margin: 0 0 4px;
}

.login__card {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.login__tabs {
  display: inline-flex;
  padding: 4px;
  border-radius: 999px;
  border: 1px solid var(--app-border);
  background: rgba(255, 255, 255, 0.02);
  width: fit-content;
  gap: 4px;
}

.login__tab {
  border: none;
  background: transparent;
  color: var(--app-text-muted);
  padding: 8px 16px;
  border-radius: 999px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.2s ease, color 0.2s ease;
}

.login__tab.active {
  background: var(--app-primary-muted);
  color: var(--app-text);
}

.login__card-header h2 {
  margin: 8px 0;
  font-weight: 600;
}

.login__card-header p {
  margin: 0;
}

.login__status {
  padding: 12px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  margin-bottom: 4px;
  background: rgba(255, 255, 255, 0.04);
}

.login__form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.login__note {
  margin: 0;
  font-size: 13px;
  color: var(--app-text-muted);
}

.pill.small {
  padding: 3px 10px;
  font-size: 11px;
}
</style>
