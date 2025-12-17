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
    <div class="sidebar-card__stats">
      <div>
        <p>참여 그룹</p>
        <strong>{{ groupStore.groups.length }}</strong>
      </div>
      <div>
        <p>선택됨</p>
        <strong>{{ selectedGroupLabel }}</strong>
      </div>
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

const selectedGroupLabel = computed(
  () => groupStore.currentGroup?.name ?? '전체'
)

function groupMeta(groupId: string) {
  const role = groupStore.roleForGroup(groupId)
  if (role === 'owner') return '역할: Owner'
  if (role === 'member') return '역할: Member'
  return '역할 확인 중'
}
</script>

<style scoped>
.sidebar-card {
  background: rgba(12, 14, 32, 0.75);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: var(--app-radius);
  padding: 22px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  box-shadow: var(--app-shadow-soft);
  backdrop-filter: blur(18px);
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

.sidebar-card__stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  padding: 16px;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.02);
}

.sidebar-card__stats p {
  margin: 0;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--app-text-muted);
}

.sidebar-card__stats strong {
  font-size: 18px;
  font-weight: 600;
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
  padding: 16px;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  cursor: pointer;
  transition: border 0.2s ease, background 0.2s ease, transform 0.2s;
  background: rgba(255, 255, 255, 0.01);
}

.sidebar-item:hover {
  border-color: rgba(255, 255, 255, 0.18);
}

.sidebar-item.active {
  border-color: rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.08);
  transform: translateY(-1px);
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
  border-radius: 16px;
  border: 1px dashed rgba(255, 255, 255, 0.1);
  padding: 24px;
  text-align: center;
  color: var(--app-text-muted);
  background: rgba(255, 255, 255, 0.01);
}
</style>
