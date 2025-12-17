<template>
  <div class="workspace" :class="{ card: useCard }">
    <div class="workspace__glow" aria-hidden="true"></div>
    <div class="workspace__icon">
      <span>WS</span>
    </div>
    <div class="workspace__body">
      <div class="workspace__title">현재 작업 공간</div>
      <div class="workspace__status">
        <template v-if="showInitialLoader">그룹 정보를 불러오는 중입니다...</template>
        <template v-else-if="!groupStore.groups.length">
          아직 그룹이 없습니다. 새 그룹을 생성하거나 초대를 받아주세요.
        </template>
        <template v-else-if="!groupStore.currentGroup">
          작업할 그룹을 선택하면 이 영역이 업데이트됩니다.
        </template>
        <template v-else>
          <div class="workspace__name">{{ groupStore.currentGroup?.name }}</div>
          <div class="workspace__meta">ID: {{ groupStore.currentGroup?.id }}</div>
        </template>
      </div>
    </div>
    <RouterLink class="btn btn-outline" to="/app/groups">그룹 관리</RouterLink>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'

import { useGroupStore } from '../../stores/group'

const props = defineProps<{ useCard?: boolean }>()

const groupStore = useGroupStore()

const showInitialLoader = computed(
  () => groupStore.isFetching && !groupStore.groups.length
)
</script>

<style scoped>
.workspace {
  position: relative;
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 24px;
  border-radius: var(--app-radius);
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: linear-gradient(120deg, rgba(255, 255, 255, 0.05), rgba(67, 83, 255, 0.12));
  overflow: hidden;
}

.workspace.card {
  background: var(--app-surface);
}

.workspace__glow {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 20% 50%, rgba(134, 99, 255, 0.5), transparent 45%);
  pointer-events: none;
  opacity: 0.7;
}

.workspace__icon {
  width: 60px;
  height: 60px;
  border-radius: 18px;
  background: linear-gradient(135deg, #8370ff, #5dd7ff);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
  color: #0b091c;
}

.workspace__body {
  position: relative;
}

.workspace__title {
  font-weight: 600;
  margin-bottom: 4px;
  letter-spacing: 0.02em;
}

.workspace__status {
  color: var(--app-text-muted);
}

.workspace__name {
  font-weight: 600;
  color: var(--app-text);
  font-size: 18px;
}

.workspace__meta {
  font-size: 13px;
  color: var(--app-text-muted);
}
</style>
