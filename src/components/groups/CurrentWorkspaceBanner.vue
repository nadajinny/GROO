<template>
  <div class="workspace" :class="{ card: useCard }">
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
    <RouterLink class="btn btn-muted" to="/app/groups">그룹 관리</RouterLink>
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
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: var(--app-radius);
  border: 1px solid var(--app-border);
  background: rgba(255, 255, 255, 0.02);
}

.workspace.card {
  background: var(--app-surface);
}

.workspace__icon {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.workspace__title {
  font-weight: 600;
  margin-bottom: 4px;
}

.workspace__status {
  color: var(--app-text-muted);
}

.workspace__name {
  font-weight: 600;
  color: var(--app-text);
}

.workspace__meta {
  font-size: 13px;
  color: var(--app-text-muted);
}
</style>
