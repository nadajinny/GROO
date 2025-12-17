<template>
  <div class="features-page">
    <div class="features-page__header">
      <p class="pill">GROO FEATURES</p>
      <h1>기능 살펴보기</h1>
      <p class="text-muted">
        대시보드·그룹·프로젝트·메시징까지, 실험실 협업을 위한 모든 기능을 한곳에서 확인하세요.
      </p>
      <RouterLink class="btn btn-primary" to="/login">지금 로그인</RouterLink>
    </div>

    <div class="feature-sections">
      <article
        v-for="(feature, index) in features"
        :key="feature.title"
        class="feature-section"
        :class="[`feature-section--${feature.theme}`]"
      >
        <div class="feature-section__content">
          <span class="section-index">{{ index + 1 }}</span>
          <h2>{{ feature.title }}</h2>
          <p>{{ feature.description }}</p>
          <ul>
            <li v-for="detail in feature.details" :key="detail">{{ detail }}</li>
          </ul>
        </div>
        <div class="feature-section__visual">
          <div class="screen-mock" :class="[`screen-mock--${feature.theme}`]">
            <div v-for="(block, idx) in feature.preview" :key="block.title" class="mock-card">
              <div class="mock-card__badge">{{ block.tag }}</div>
              <h3>{{ block.title }}</h3>
              <p>{{ block.description }}</p>
            </div>
          </div>
        </div>
      </article>
    </div>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'

const features = [
  {
    title: '실시간 대시보드',
    description: '개인 일정과 프로젝트 알림을 한 화면에서 확인하고, 우선순위를 즉시 조정합니다.',
    details: [
      '마감일과 상태별로 태스크를 정렬',
      '서브태스크와 활동 로그를 실시간 반영',
      '알림 센터에서 프로젝트 이벤트 확인'
    ],
    preview: [
      { tag: '일정', title: '마감 일정', description: '오늘 마감되는 태스크: 실험계획 초안 검토' },
      { tag: '알림', title: '프로젝트 업데이트', description: '신규 데이터셋이 업로드되었습니다.' }
    ],
    theme: 'dashboard'
  },
  {
    title: '그룹 및 멤버 관리',
    description: 'Owner와 Member 권한을 분리해 민감한 데이터 접근을 안전하게 제어할 수 있습니다.',
    details: [
      '그룹별 멤버 초대·권한 변경·제거',
      '태그/핸들 기반 검색으로 빠른 찾기',
      '현재 작업 그룹을 고정해 뷰 제한'
    ],
    preview: [
      { tag: '그룹', title: '바이오 연구팀', description: 'Owner · Members · 초대 대기중 2명' },
      { tag: '멤버', title: '김지훈 · Owner', description: '권한: 프로젝트 관리, 태스크 편집' }
    ],
    theme: 'groups'
  },
  {
    title: '프로젝트 & 태스크',
    description: '프로젝트별 파일·노트·태스크를 한 번에 관리하고, 세부 변경 이력을 기록합니다.',
    details: [
      '태스크 우선순위/상태/담당자 필터링',
      '프로젝트 노트·파일과 연결된 히스토리',
      '활동 로그와 댓글로 협업 기록 남기기'
    ],
    preview: [
      { tag: '태스크', title: '실험 보고서 초안', description: '우선순위: HIGH · 진행상태: DOING' },
      { tag: '파일', title: '실험 결과.pdf', description: '업로드: 김지훈 · 12MB' }
    ],
    theme: 'projects'
  },
  {
    title: '메시징 & 알림',
    description: '그룹 채팅과 1:1 대화를 모두 지원하며, 프로젝트 업데이트를 즉시 전달합니다.',
    details: [
      '그룹 채널과 개인 채널을 동시에 이용',
      '멘션/알림으로 놓치는 메시지 없이 공유',
      '실험 결과 공유용 파일 전송 지원'
    ],
    preview: [
      { tag: '채널', title: '#실험-알림', description: '김지훈: “오늘 2시 결과 공유 모임입니다.”' },
      { tag: 'DM', title: '이하늘', description: '“새 데이터셋 확인 부탁드려요.”' }
    ],
    theme: 'messages'
  }
]
</script>

<style scoped>
.features-page {
  min-height: 100vh;
  padding: clamp(32px, 6vw, 80px) clamp(20px, 6vw, 80px) 120px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.features-page__header {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 720px;
}

.feature-sections {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.feature-section {
  border-radius: 24px;
  padding: clamp(20px, 5vw, 36px);
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  position: relative;
  overflow: hidden;
  animation: fade-up 0.6s ease forwards;
  opacity: 0;
  transform: translateY(24px);
}

.feature-section--dashboard {
  background: linear-gradient(135deg, rgba(117, 97, 255, 0.15), rgba(17, 10, 45, 0.9));
}

.feature-section--groups {
  background: linear-gradient(135deg, rgba(54, 191, 255, 0.18), rgba(11, 19, 33, 0.9));
}

.feature-section--projects {
  background: linear-gradient(135deg, rgba(255, 174, 113, 0.2), rgba(26, 13, 2, 0.9));
}

.feature-section--messages {
  background: linear-gradient(135deg, rgba(255, 111, 183, 0.16), rgba(28, 6, 16, 0.9));
}

.feature-section__content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.feature-section__content ul {
  padding-left: 18px;
  margin: 0;
  color: var(--app-text-muted);
}

.feature-section__visual {
  display: flex;
  align-items: center;
  justify-content: center;
}

.screen-mock {
  width: min(420px, 100%);
  border-radius: 24px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.screen-mock--dashboard {
  box-shadow: 0 30px 60px rgba(63, 55, 143, 0.4);
}

.screen-mock--groups {
  box-shadow: 0 30px 60px rgba(45, 139, 255, 0.25);
}

.screen-mock--projects {
  box-shadow: 0 30px 60px rgba(240, 128, 78, 0.25);
}

.screen-mock--messages {
  box-shadow: 0 30px 60px rgba(233, 123, 190, 0.25);
}

.mock-card {
  padding: 18px;
  border-radius: 18px;
  background: rgba(0, 0, 0, 0.35);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.mock-card__badge {
  display: inline-flex;
  font-size: 12px;
  letter-spacing: 0.08em;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  margin-bottom: 10px;
}

.section-index {
  font-size: 14px;
  letter-spacing: 0.15em;
  color: var(--app-text-muted);
}

@keyframes fade-up {
  from {
    opacity: 0;
    transform: translateY(24px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 640px) {
  .screen-mock {
    padding: 12px;
  }
}
</style>
