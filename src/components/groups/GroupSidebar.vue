<template>
  <div class="sidebar-card">
    <div class="sidebar-card__header">
      <div>
        <div class="sidebar-card__title">내 그룹</div>
        <div class="sidebar-card__subtitle">
          최대 10개의 최근 그룹이 표시됩니다.
        </div>
      </div>
      <RouterLink class="btn btn-muted" :to="currentGroupLink">
        전체보기
      </RouterLink>
    </div>
    <div v-if="groupStore.isFetching && !groupStore.groups.length" class="empty">
      그룹 정보를 불러오는 중...
    </div>
    <div v-else-if="!groupStore.groups.length" class="empty">
      참여 중인 그룹이 없습니다. 새 그룹을 만들어보세요.
    </div>
    <ul v-else class="list-reset sidebar-list">
      <li
        :class="['sidebar-item', { active: groupStore.currentGroupId === null }]"
        @click="groupStore.viewAllGroups"
      >
        <div>
          <div class="sidebar-item__title">전체 보기</div>
          <div class="sidebar-item__meta">모든 그룹 기준으로 보기</div>
        </div>
        <span class="pill">전체</span>
      </li>
      <li
        v-for="group in visibleGroups"
        :key="group.id"
        :class="['sidebar-item', { active: groupStore.currentGroupId === group.id }]"
        @click="groupStore.selectGroup(group.id)"
      >
        <div>
          <div class="sidebar-item__title">{{ group.name }}</div>
          <div class="sidebar-item__meta">
            {{ groupMeta(group.id) }}
          </div>
        </div>
        <span class="pill">ID {{ group.id.slice(0, 5) }}</span>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'

import { useGroupStore } from '../../stores/group'

const groupStore = useGroupStore()

const visibleGroups = computed(() => groupStore.groups.slice(0, 10))

const currentGroupLink = computed(() => {
  const selectedId = groupStore.currentGroupId
  return selectedId ? `/app/groups/${selectedId}` : '/app/groups'
})

function groupMeta(groupId: string) {
  const role = groupStore.roleForGroup(groupId)
  if (role === 'owner') return '역할: Owner'
  if (role === 'member') return '역할: Member'
  return '역할 확인 중'
}
</script>

<style scoped>
.sidebar-card {
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius);
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.sidebar-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.sidebar-card__title {
  font-weight: 600;
}

.sidebar-card__subtitle {
  color: var(--app-text-muted);
  font-size: 13px;
}

.sidebar-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.sidebar-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px;
  border-radius: 14px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: border 0.2s ease, background 0.2s ease;
}

.sidebar-item:hover {
  border-color: var(--app-border-light);
}

.sidebar-item.active {
  border-color: rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.04);
}

.sidebar-item__title {
  font-weight: 600;
  font-size: 15px;
}

.sidebar-item__meta {
  color: var(--app-text-muted);
  font-size: 13px;
}

.empty {
  border-radius: 14px;
  border: 1px dashed var(--app-border);
  padding: 24px;
  text-align: center;
  color: var(--app-text-muted);
}
</style>
