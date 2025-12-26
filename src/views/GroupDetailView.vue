<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-header__title">{{ group?.name ?? '그룹 상세' }}</h2>
      <p class="page-header__subtitle">
        {{ group ? '선택한 그룹의 작업 공간 정보와 멤버 목록을 확인하세요.' : '그룹 정보를 불러오는 중입니다.' }}
      </p>
      <div class="card-actions">
        <RouterLink class="btn btn-muted" to="/app/groups">그룹 목록</RouterLink>
        <button
          v-if="group"
          class="btn btn-primary"
          :disabled="groupStore.currentGroupId === group.id"
          @click="selectGroup(group)"
        >
          {{ groupStore.currentGroupId === group.id ? '현재 사용 중' : '현재 작업 그룹으로 설정' }}
        </button>
      </div>
    </div>

    <section class="page-section">
      <CurrentWorkspaceBanner :use-card="false" />
    </section>

    <section v-if="group" class="page-section grid-two">
      <article class="app-card">
        <h3>그룹 정보</h3>
        <p class="text-muted">그룹 ID: {{ group.id }}</p>
        <p class="text-muted">
          생성일:
          {{ group.createdAt ? formatDateTime(group.createdAt) : '서버 시간 대기중' }}
        </p>
        <p class="text-muted">내 역할: {{ roleLabel }}</p>
        <p v-if="!groupStore.canManageGroup(group.id)" class="text-muted">
          Owner 권한이 있는 사용자만 멤버를 관리할 수 있습니다.
        </p>
      </article>
      <GroupMembersPanel :group="group" />
    </section>

    <section v-else class="page-section">
      <div class="empty-state">
        그룹을 찾을 수 없습니다. 목록으로 돌아가 다시 선택해주세요.
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, RouterLink } from 'vue-router'

import CurrentWorkspaceBanner from '../components/groups/CurrentWorkspaceBanner.vue'
import GroupMembersPanel from '../components/groups/GroupMembersPanel.vue'
import { useGroupStore } from '../stores/group'

const route = useRoute()
const groupStore = useGroupStore()

const groupId = computed(() => route.params.groupId as string)
const group = computed(() => groupStore.groups.find((item) => item.id === groupId.value) ?? null)

const roleLabel = computed(() => {
  const role = group.value ? groupStore.roleForGroup(group.value.id) : null
  if (role === 'OWNER') return 'Owner'
  if (role === 'ADMIN') return 'Admin'
  if (role === 'MEMBER') return 'Member'
  return '확인 중'
})

function selectGroup(group: any) {
  groupStore.selectGroup(group.id)
}

function formatDateTime(date: Date) {
  const pad = (value: number) => `${value}`.padStart(2, '0')
  return `${date.getFullYear()}.${pad(date.getMonth() + 1)}.${pad(date.getDate())} ${pad(
    date.getHours()
  )}:${pad(date.getMinutes())}`
}
</script>
