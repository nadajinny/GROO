<template>
  <div class="app-card members-panel">
    <div class="members-panel__header">
      <div>
        <div class="members-panel__title">{{ group.name }} 멤버</div>
        <div class="members-panel__meta">
          총 {{ members?.length ?? 0 }}명
        </div>
      </div>
      <button class="btn btn-muted" :disabled="isLoading" @click="refresh">
        새로고침
      </button>
    </div>
    <div v-if="isLoading && !members?.length" class="empty">
      멤버 정보를 불러오는 중입니다...
    </div>
    <div v-else-if="hasError && !members?.length" class="empty">
      멤버 목록을 불러오지 못했습니다.
      <button class="btn btn-primary" @click="refresh">다시 시도</button>
    </div>
    <ul v-else class="list-reset member-list">
      <li v-for="member in members" :key="member.userId" class="member-item">
        <div class="avatar">{{ member.handle?.[0]?.toUpperCase() ?? 'U' }}</div>
        <div class="member-info">
          <div class="member-handle">{{ member.handle ?? '알 수 없음' }}</div>
          <div class="member-role">
            역할: {{ member.role === 'owner' ? 'Owner' : 'Member' }}
            <span v-if="member.userId === currentUserId">· 나</span>
          </div>
        </div>
        <span v-if="member.role === 'owner'" class="badge badge-warning">OWNER</span>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useGroupStore } from '../../stores/group'
import { useAuthStore } from '../../stores/auth'
import type { Group } from '../../types'

const props = defineProps<{ group: Group }>()

const groupStore = useGroupStore()
const authStore = useAuthStore()

const members = computed(() => groupStore.membersForGroup(props.group.id))
const isLoading = computed(() => groupStore.isFetchingMembers(props.group.id))
const hasError = computed(() => groupStore.hasMemberLoadError(props.group.id))
const currentUserId = computed(() => authStore.user?.uid)

function refresh() {
  groupStore.fetchMembersOfGroup(props.group.id, { force: true })
}

onMounted(() => {
  refresh()
})

watch(
  () => props.group.id,
  () => refresh()
)
</script>

<style scoped>
.members-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}

.members-panel__title {
  font-weight: 600;
}

.members-panel__meta {
  color: var(--app-text-muted);
  font-size: 13px;
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 14px;
  border: 1px solid var(--app-border);
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.member-info {
  flex: 1;
}

.member-handle {
  font-weight: 600;
}

.member-role {
  color: var(--app-text-muted);
  font-size: 13px;
}

.empty {
  border: 1px dashed var(--app-border);
  border-radius: var(--app-radius);
  padding: 24px;
  text-align: center;
  color: var(--app-text-muted);
}
</style>
