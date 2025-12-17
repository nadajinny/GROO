<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-header__title">그룹 관리</h2>
      <p class="page-header__subtitle">
        내가 속한 팀/그룹을 조회하고 직접 생성할 수 있는 화면입니다.
      </p>
      <div class="card-actions">
        <button class="btn btn-primary" :disabled="groupStore.isCreating" @click="openCreateDialog">
          {{ groupStore.isCreating ? '생성 중...' : '새 그룹 만들기' }}
        </button>
        <button class="btn btn-outline" :disabled="groupStore.isFetching" @click="groupStore.fetchMyGroups()">
          새로고침
        </button>
      </div>
    </div>

    <section class="page-section">
      <CurrentWorkspaceBanner :use-card="false" />
    </section>

    <section class="page-section app-card">
      <div class="section-header">
        <div>
          <h3>내 그룹 목록</h3>
          <p class="text-muted">
            목록을 새로고침하거나 필요한 그룹의 멤버를 빠르게 관리하세요.
          </p>
        </div>
      </div>
      <div v-if="groupStore.errorMessage" class="error-banner">
        {{ groupStore.errorMessage }}
      </div>
      <div v-if="groupStore.isFetching && !groupStore.groups.length" class="empty-state">
        그룹을 불러오는 중입니다...
      </div>
      <div v-else-if="!groupStore.groups.length" class="empty-state">
        아직 참여 중인 그룹이 없습니다. 새 그룹을 만들어보세요.
      </div>
      <TransitionGroup v-else tag="div" name="list-fade" class="stack">
        <article v-for="group in groupStore.groups" :key="group.id" class="card-with-border group-card">
          <header>
            <div>
              <h4>{{ group.name }}</h4>
              <p class="text-muted">
                생성일:
                {{ group.createdAt ? formatDateTime(group.createdAt) : '서버 시간 대기중' }}
              </p>
            </div>
            <button class="btn btn-muted" @click="goToGroupDetail(group.id)">
              상세 보기
            </button>
          </header>
          <p class="text-muted">ID {{ group.id }}</p>
          <div class="card-actions">
            <button class="btn btn-outline" @click="selectGroup(group)">
              {{ groupStore.currentGroupId === group.id ? '선택됨' : '현재로 설정' }}
            </button>
            <button
              v-if="groupStore.canManageGroup(group.id)"
              class="btn btn-muted"
              :disabled="groupStore.isAddingMember(group.id)"
              @click="openAddMemberDialog(group)"
            >
              {{ groupStore.isAddingMember(group.id) ? '추가 중...' : '멤버 추가' }}
            </button>
          </div>
        </article>
      </TransitionGroup>
    </section>

    <div v-if="createDialogOpen" class="dialog-backdrop">
      <div class="dialog">
        <h3>새 그룹 만들기</h3>
        <input
          v-model="newGroupName"
          class="input"
          type="text"
          placeholder="예: 데이터 분석팀"
        />
        <div class="dialog-actions">
          <button class="btn btn-outline" @click="closeCreateDialog">취소</button>
          <button class="btn btn-primary" @click="submitCreateGroup">생성</button>
        </div>
      </div>
    </div>

    <div v-if="memberDialogGroup" class="dialog-backdrop">
      <div class="dialog">
        <h3>멤버 추가 · {{ memberDialogGroup.name }}</h3>
        <input
          v-model="memberHandle"
          class="input"
          type="text"
          placeholder="예: username#12345"
        />
        <div class="dialog-actions">
          <button class="btn btn-outline" @click="closeMemberDialog">취소</button>
          <button class="btn btn-primary" @click="submitAddMember">추가</button>
        </div>
        <p v-if="memberError" class="text-muted">{{ memberError }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

import CurrentWorkspaceBanner from '../components/groups/CurrentWorkspaceBanner.vue'
import { useGroupStore } from '../stores/group'
import * as userDirectory from '../services/userDirectory'
import type { Group } from '../types'

const groupStore = useGroupStore()
const router = useRouter()

const createDialogOpen = ref(false)
const newGroupName = ref('')
const memberDialogGroup = ref<Group | null>(null)
const memberHandle = ref('')
const memberError = ref<string | null>(null)

function openCreateDialog() {
  createDialogOpen.value = true
  newGroupName.value = ''
}

function closeCreateDialog() {
  createDialogOpen.value = false
}

async function submitCreateGroup() {
  const id = await groupStore.createGroup(newGroupName.value)
  if (id) {
    createDialogOpen.value = false
    newGroupName.value = ''
  }
}

function selectGroup(group: Group) {
  const changed = groupStore.selectGroup(group.id)
  if (!changed) return
}

function goToGroupDetail(groupId: string) {
  router.push(`/app/groups/${groupId}`)
}

function openAddMemberDialog(group: Group) {
  memberDialogGroup.value = group
  memberHandle.value = ''
  memberError.value = null
}

function closeMemberDialog() {
  memberDialogGroup.value = null
  memberHandle.value = ''
  memberError.value = null
}

async function submitAddMember() {
  if (!memberDialogGroup.value) return
  const profile = await userDirectory.fetchByHandle(memberHandle.value)
  if (!profile) {
    memberError.value = '해당 핸들을 가진 사용자를 찾을 수 없습니다.'
    return
  }
  const success = await groupStore.addMemberToGroup({
    group: memberDialogGroup.value,
    memberUserId: profile.uid
  })
  if (success) {
    closeMemberDialog()
  } else if (groupStore.errorMessage) {
    memberError.value = groupStore.errorMessage
  }
}

function formatDateTime(date: Date) {
  const pad = (value: number) => `${value}`.padStart(2, '0')
  return `${date.getFullYear()}.${pad(date.getMonth() + 1)}.${pad(date.getDate())} ${pad(
    date.getHours()
  )}:${pad(date.getMinutes())}`
}
</script>

<style scoped>
.group-card header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.group-card h4 {
  margin: 0;
}

.dialog-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.dialog {
  width: min(420px, 90vw);
  background: var(--app-surface);
  border-radius: var(--app-radius);
  border: 1px solid var(--app-border);
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.error-banner {
  border-radius: 12px;
  padding: 12px;
  background: rgba(255, 107, 107, 0.15);
  border: 1px solid rgba(255, 107, 107, 0.4);
  margin-bottom: 16px;
}

.list-fade-enter-active,
.list-fade-leave-active {
  transition: all 0.25s ease;
}

.list-fade-enter-from,
.list-fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
