<template>
  <div class="home">
    <div class="home__glow" aria-hidden="true"></div>
    <section class="home__hero">
      <p class="pill">GROO LAB TOOL</p>
      <h1>실험실 협업을 위한 완전한 워크스페이스</h1>
      <p class="home__lead">
        그룹 관리, 프로젝트와 태스크 추적, 메시징까지 모두 한 화면에서 연결합니다.
        로그인 없이도 기능 미리보기를 확인하고, 이메일 기반 계정으로 바로 시작하세요.
      </p>
      <div class="home__actions">
        <RouterLink class="btn btn-primary" to="/login">무료로 시작하기</RouterLink>
        <RouterLink class="btn btn-muted" to="/features">기능 살펴보기</RouterLink>
      </div>
    </section>

    <RouterLink class="home__features-card app-card" to="/features">
      <div>
        <div class="section-header">
          <div>
            <h2>핵심 기능</h2>
            <p class="text-muted">모든 기능을 자세히 살펴보시겠어요?</p>
          </div>
          <span class="btn btn-outline">기능 페이지로 이동</span>
        </div>
        <div class="features-grid">
          <article
            v-for="(feature, index) in features"
            :key="feature.title"
            class="feature-card"
            :style="{ animationDelay: `${index * 120}ms` }"
          >
            <div class="feature-icon">{{ feature.icon }}</div>
            <div class="feature-card__body">
              <div class="feature-card__head">
                <h3>{{ feature.title }}</h3>
                <p>{{ feature.description }}</p>
              </div>
              <ul class="feature-details">
                <li v-for="detail in feature.details" :key="detail">
                  <span class="dot" />
                  <span>{{ detail }}</span>
                </li>
              </ul>
            </div>
          </article>
        </div>
      </div>
    </RouterLink>

    <section class="home__cta app-card">
      <div>
        <h2>지금 바로 협업을 시작해보세요</h2>
        <p class="text-muted">
          로그인 후 대시보드에서 그룹과 프로젝트를 선택하면 맞춤형 업무 현황을 실시간으로 보여줍니다.
        </p>
      </div>
      <RouterLink class="btn btn-primary" to="/login">로그인하고 대시보드 열기</RouterLink>
    </section>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'

const features = [
  {
    icon: '01',
    title: '실시간 대시보드',
    description: '그룹과 프로젝트별 마감 일정, 알림, 태스크를 실시간으로 집계합니다.',
    details: [
      '마감일 기준으로 개인 태스크를 자동 정렬',
      '프로젝트 업데이트와 알림을 하이라이트로 표시',
      '서브태스크 완료 현황까지 한 번에 확인'
    ]
  },
  {
    icon: '02',
    title: '권한 기반 그룹 관리',
    description: 'Owner와 Member 권한을 분리하여 민감한 데이터 접근을 안전하게 제어합니다.',
    details: [
      '그룹별 멤버 초대·추가·제거를 실시간 적용',
      'Owner 전용 기능으로 프로젝트/멤버 권한 제어',
      '멤버 리스트를 태그·역할별로 빠르게 탐색'
    ]
  },
  {
    icon: '03',
    title: '프로젝트 & 태스크',
    description: '프로젝트의 세부 태스크·서브태스크·활동 로그까지 모두 기록하고 공유합니다.',
    details: [
      '프로젝트별 파일·노트·태스크를 통합 관리',
      '우선순위와 상태별 필터링으로 업무 집중',
      '활동 로그/댓글로 변경 이력을 투명하게 기록'
    ]
  },
  {
    icon: '04',
    title: '메시징 & 알림',
    description: '그룹 채널과 1:1 대화 모두 지원하며, 업데이트를 즉시 전달합니다.',
    details: [
      '그룹 대화방과 개인 대화방을 동시에 이용',
      '읽지 않은 메시지와 멘션을 강조 표시',
      '알림 센터에서 프로젝트 이벤트를 빠르게 확인'
    ]
  }
]
</script>

<style scoped>
.home {
  min-height: 100vh;
  padding: clamp(32px, 6vw, 80px) clamp(20px, 6vw, 80px) 100px;
  display: flex;
  flex-direction: column;
  gap: 48px;
  position: relative;
}

.home__glow {
  position: absolute;
  inset-inline: 0;
  top: 0;
  height: 360px;
  background: radial-gradient(circle at 20% 20%, rgba(139, 115, 255, 0.35), transparent 55%),
    radial-gradient(circle at 80% 10%, rgba(70, 170, 255, 0.25), transparent 50%);
  pointer-events: none;
}

.home__hero {
  position: relative;
  max-width: 760px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.home__hero h1 {
  font-size: clamp(40px, 7vw, 64px);
  margin: 0;
  line-height: 1.08;
  letter-spacing: -0.01em;
}

.home__lead {
  color: var(--app-text-muted);
  font-size: 18px;
  margin: 0;
}

.home__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
}

.home__features {
  position: relative;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.feature-card {
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 18px;
  padding: 18px;
  background: rgba(255, 255, 255, 0.02);
  display: flex;
  gap: 16px;
  position: relative;
  overflow: hidden;
  animation: fade-up 0.65s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
  transform: translateY(30px);
}

.feature-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(140deg, rgba(255, 255, 255, 0.08), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.feature-card:hover::after {
  opacity: 1;
}

.feature-card__body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.feature-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: var(--app-primary);
  margin-top: 6px;
  flex-shrink: 0;
}

.feature-card__head h3 {
  margin: 0 0 6px;
}

.feature-details {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.feature-details li {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  color: var(--app-text-muted);
  font-size: 14px;
}

.feature-details .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--app-primary);
  margin-top: 6px;
}

.home__cta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.home__cta h2 {
  margin: 0 0 8px;
}

@media (max-width: 640px) {
  .home__actions {
    flex-direction: column;
  }

  .home__cta {
    flex-direction: column;
    align-items: flex-start;
  }
}

@keyframes fade-up {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
