<template>
  <div class="login">
    <div class="login__background" aria-hidden="true"></div>
    <section class="login__hero">
      <span class="pill">Lab Tool Web</span>
      <h1>GROO로 실험실 협업을 한 번에</h1>
      <p>
        Google 계정 한 번의 로그인만으로 프로젝트, 태스크, 그룹, 메신저까지
        모두 관리할 수 있도록 Lab Tool에서 제공하던 기능을 그대로 옮겨왔습니다.
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
        <h2>계정을 연결하고 협업을 시작하세요</h2>
        <p class="text-muted">
          Firebase Authentication · Cloud Firestore 환경을 그대로 사용합니다.
        </p>
      </div>
      <div class="login__status" v-if="authStore.errorMessage">
        {{ authStore.errorMessage }}
      </div>
      <button class="btn btn-primary" :disabled="authStore.isLoading" @click="authStore.signInWithGoogle">
        {{ authStore.isLoading ? '로그인 중...' : 'Google 계정으로 로그인' }}
      </button>
      <p class="login__note">
        로그인하면 Lab Tool 계정 정보와 동기화되어 그룹·프로젝트·메시지를 그대로 사용할 수 있습니다.
      </p>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()

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
