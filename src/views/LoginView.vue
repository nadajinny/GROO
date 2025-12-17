<template>
  <div class="login">
    <div class="login__hero">
      <div class="pill">Lab Tool Web</div>
      <h1>GROO로 실험실 협업을 한 번에</h1>
      <p>
        Google 계정 한 번의 로그인만으로 프로젝트, 태스크, 그룹, 메신저까지
        모두 관리할 수 있도록 Lab Tool에서 제공하던 기능을 그대로 옮겨왔습니다.
      </p>
      <ul>
        <li><span>·</span> 대시보드에서 마감 일정과 알림을 한눈에 확인</li>
        <li><span>·</span> 그룹별 권한, 멤버, 프로젝트를 실시간 관리</li>
        <li><span>·</span> 태스크 세부정보·서브태스크·댓글 기록까지 제공</li>
      </ul>
    </div>
    <div class="login__card app-card">
      <h2>계정 연결</h2>
      <p class="text-muted">
        Firebase Authentication · Cloud Firestore 환경을 그대로 사용합니다.
      </p>
      <div class="login__status" v-if="authStore.errorMessage">
        {{ authStore.errorMessage }}
      </div>
      <button class="btn btn-primary" :disabled="authStore.isLoading" @click="authStore.signInWithGoogle">
        {{ authStore.isLoading ? '로그인 중...' : 'Google 계정으로 로그인' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
</script>

<style scoped>
.login {
  min-height: 100vh;
  padding: clamp(32px, 6vw, 80px) clamp(20px, 6vw, 80px);
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 32px;
  align-items: center;
}

.login__hero h1 {
  font-size: clamp(32px, 6vw, 48px);
  margin: 16px 0;
}

.login__hero p {
  color: var(--app-text-muted);
  max-width: 560px;
}

.login__hero ul {
  list-style: none;
  padding: 0;
  margin: 24px 0 0;
  color: var(--app-text-muted);
}

.login__hero li {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}

.login__card h2 {
  margin: 0 0 8px;
  font-weight: 600;
}

.login__card p {
  margin: 0 0 24px;
}

.login__status {
  padding: 12px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  margin-bottom: 16px;
}
</style>
