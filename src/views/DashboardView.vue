<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-header__title">대시보드</h2>
      <p class="page-header__subtitle">
        나에게 배정된 태스크, 예정된 마감일, 프로젝트 알림을 한 화면에서 확인하세요.
      </p>
    </div>
    <CurrentWorkspaceBanner :use-card="false" class="page-section" />

    <div class="dashboard-grid">
      <section class="app-card">
        <div class="section-header">
          <div>
            <h3>일정 캘린더</h3>
            <p class="text-muted">마감일 기준으로 할당된 태스크를 확인합니다.</p>
          </div>
          <button class="btn btn-muted" :disabled="isLoading" @click="loadDashboard">
            새로고침
          </button>
        </div>
        <div v-if="errorMessage" class="empty-state">
          {{ errorMessage }}
        </div>
        <div v-else-if="isLoading" class="empty-state">
          데이터를 불러오는 중입니다...
        </div>
        <div v-else>
          <div v-if="!datedGroups.length && !undatedTasks.length" class="empty-state">
            마감일이 등록된 태스크가 없습니다.
          </div>
          <div class="calendar-list" v-else>
            <div v-for="(tasks, day) in datedGroups" :key="day" class="calendar-day">
              <div class="calendar-day__header">
                <div class="calendar-day__date">{{ day }}</div>
                <span class="badge badge-success">{{ tasks.length }}건</span>
              </div>
              <ul class="list-reset">
                <li v-for="task in tasks" :key="task.task.id" class="calendar-task">
                  <div>
                    <div class="calendar-task__title">{{ task.task.title }}</div>
                    <div class="calendar-task__meta">
                      {{ task.group.name }} · {{ task.project.name }}
                    </div>
                  </div>
                  <div class="calendar-task__tags">
                    <span class="chip">{{ task.task.status }}</span>
                    <span class="chip">{{ task.task.priority }}</span>
                  </div>
                </li>
              </ul>
            </div>
            <div v-if="undatedTasks.length" class="calendar-day">
              <div class="calendar-day__header">
                <div class="calendar-day__date">마감일 미지정</div>
                <span class="badge badge-warning">{{ undatedTasks.length }}건</span>
              </div>
              <ul class="list-reset">
                <li v-for="task in undatedTasks" :key="task.task.id" class="calendar-task">
                  <div>
                    <div class="calendar-task__title">{{ task.task.title }}</div>
                    <div class="calendar-task__meta">
                      {{ task.group.name }} · {{ task.project.name }}
                    </div>
                  </div>
                  <span class="chip">{{ task.task.status }}</span>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </section>

      <section class="app-card">
        <div class="section-header">
          <div>
            <h3>나의 할 일</h3>
            <p class="text-muted">
              선택된 범위 내에서 나에게 배정된 태스크와 서브태스크 목록
            </p>
          </div>
        </div>
        <div v-if="isLoading" class="empty-state">태스크를 불러오는 중입니다...</div>
        <div v-else-if="!assignedTasks.length" class="empty-state">
          현재 할당된 태스크가 없습니다.
        </div>
        <div v-else class="stack">
          <article
            v-for="task in assignedTasks"
            :key="task.task.id"
            class="task-card"
          >
            <header>
              <div>
                <h4>{{ task.task.title }}</h4>
                <p class="text-muted">
                  {{ task.group.name }} · {{ task.project.name }}
                </p>
              </div>
              <div class="task-tags">
                <span class="chip">{{ task.task.status }}</span>
                <span class="chip">{{ task.task.priority }}</span>
              </div>
            </header>
            <p v-if="task.task.description" class="desc">
              {{ task.task.description }}
            </p>
            <p class="text-muted">
              마감일: {{ task.task.dueDate ? formatDate(task.task.dueDate) : '등록되지 않음' }}
            </p>
            <div class="subtasks">
              <h5>서브태스크</h5>
              <div v-if="!subtasks[task.task.id]?.length" class="text-muted">
                등록된 서브태스크가 없습니다.
              </div>
              <ul v-else class="list-reset">
                <li v-for="sub in subtasks[task.task.id]" :key="sub.id" class="subtask">
                  <label class="subtask__row">
                    <input
                      type="checkbox"
                      :checked="sub.isDone"
                      :disabled="isSubtaskLoading(task.task.id, sub.id)"
                      @change="onSubtaskToggle(task.task.id, sub, $event)"
                    />
                    <span>{{ sub.title }}</span>
                  </label>
                  <span class="chip">{{ sub.isDone ? '완료' : '진행중' }}</span>
                </li>
              </ul>
            </div>
          </article>
        </div>
      </section>

      <section class="app-card">
        <div class="section-header">
          <div>
            <h3>업데이트 알림</h3>
            <p class="text-muted">프로젝트 생성 및 태스크 변경과 같은 최신 알림</p>
          </div>
        </div>
        <div v-if="isLoading" class="empty-state">알림을 준비하는 중...</div>
        <div v-else-if="!notifications.length" class="empty-state">
          최근 업데이트된 알림이 없습니다.
        </div>
        <ul v-else class="list-reset notifications">
          <li v-for="notice in notifications" :key="notice.id">
            <button
              type="button"
              class="notification"
              @click="notificationTarget(notice.groupId)"
            >
              <div>
                <div class="notification__title">{{ notice.title }}</div>
                <div class="notification__desc">{{ notice.description }}</div>
                <div class="notification__meta">
                  {{ notice.groupName }} · {{ notice.projectName }}
                </div>
              </div>
              <div class="notification__time">
                {{ notice.timestamp ? formatTime(notice.timestamp) : '-' }}
              </div>
            </button>
          </li>
        </ul>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { format } from 'date-fns'
import { useRouter } from 'vue-router'

import CurrentWorkspaceBanner from '../components/groups/CurrentWorkspaceBanner.vue'
import { useAuthStore } from '../stores/auth'
import { useGroupStore } from '../stores/group'
import * as projectService from '../services/projectService'
import * as taskService from '../services/taskService'
import type { Group, Project, Subtask, Task } from '../types'

interface AssignedTask {
  group: Group
  project: Project
  task: Task
}

interface DashboardNotification {
  id: string
  title: string
  description: string
  groupName: string
  projectName: string
  timestamp: Date | null | undefined
  groupId: string
  projectId?: string
  taskId?: string
}

const authStore = useAuthStore()
const groupStore = useGroupStore()
const router = useRouter()

const assignedTasks = ref<AssignedTask[]>([])
const notifications = ref<DashboardNotification[]>([])
const subtasks = ref<Record<string, Subtask[]>>({})
const tasksByDay = ref<Record<string, AssignedTask[]>>({})
const undatedTasks = ref<AssignedTask[]>([])
const isLoading = ref(true)
const errorMessage = ref<string | null>(null)
const subtaskLoading = ref<Record<string, boolean>>({})

const datedGroups = computed(() => tasksByDay.value)

async function loadDashboard() {
  const userId = authStore.user?.uid
  const userHandle = authStore.profile?.handle
  if (!userId) {
    errorMessage.value = '로그인 후 이용해주세요.'
    return
  }
  isLoading.value = true
  errorMessage.value = null
  try {
    const groups = resolveTargetGroups()
    if (!groups.length) {
      assignedTasks.value = []
      notifications.value = []
      subtasks.value = {}
      tasksByDay.value = {}
      undatedTasks.value = []
      isLoading.value = false
      return
    }

    const collectedTasks: AssignedTask[] = []
    const collectedNotifications: DashboardNotification[] = []

    for (const group of groups) {
      const projects = await projectService.getProjectsForGroup(group.id)
      for (const project of projects) {
        const tasks = await taskService.getTasksForProject(project.id)
        for (const task of tasks) {
          const assignee = task.assigneeId
          const isMine =
            !!assignee &&
            (assignee === userId || (userHandle && assignee === userHandle))
          if (isMine) {
            collectedTasks.push({ group, project, task })
          }
          if (isMine || task.createdBy === userId) {
            collectedNotifications.push({
              id: `task-${task.id}`,
              title: task.title,
              description: isMine
                ? '나에게 배정된 태스크가 업데이트되었습니다.'
                : '내가 생성한 태스크에 변경이 있습니다.',
              groupName: group.name,
              projectName: project.name,
              timestamp: task.createdAt,
              groupId: group.id,
              projectId: project.id,
              taskId: task.id
            })
          }
        }
        if (project.createdAt) {
          collectedNotifications.push({
            id: `project-${project.id}`,
            title: project.name,
            description: '프로젝트가 생성되었습니다.',
            groupName: group.name,
            projectName: project.name,
            timestamp: project.createdAt,
            groupId: group.id,
            projectId: project.id
          })
        }
      }
    }

    collectedTasks.sort((a, b) => {
      const aTime = a.task.dueDate?.getTime() ?? Infinity
      const bTime = b.task.dueDate?.getTime() ?? Infinity
      return aTime - bTime
    })

    collectedNotifications.sort((a, b) => {
      const aTime = a.timestamp?.getTime() ?? 0
      const bTime = b.timestamp?.getTime() ?? 0
      return bTime - aTime
    })

    const subtaskMap: Record<string, Subtask[]> = {}
    const calendarMap: Record<string, AssignedTask[]> = {}
    const undated: AssignedTask[] = []

    for (const assigned of collectedTasks) {
      subtaskMap[assigned.task.id] = await taskService.getSubtasks(assigned.task.id)
      if (assigned.task.dueDate) {
        const dayKey = format(assigned.task.dueDate, 'yyyy.MM.dd (EEE)')
        calendarMap[dayKey] = [...(calendarMap[dayKey] ?? []), assigned]
      } else {
        undated.push(assigned)
      }
    }

    assignedTasks.value = collectedTasks
    notifications.value = collectedNotifications.slice(0, 12)
    subtasks.value = subtaskMap
    tasksByDay.value = calendarMap
    undatedTasks.value = undated
  } catch (error) {
    console.error('Failed to load dashboard', error)
    errorMessage.value = '대시보드 데이터를 불러오지 못했습니다.'
  } finally {
    isLoading.value = false
  }
}

function resolveTargetGroups(): Group[] {
  if (!groupStore.groups.length) return []
  if (!groupStore.currentGroupId) return groupStore.groups
  return groupStore.groups.filter((group) => group.id === groupStore.currentGroupId)
}

function formatDate(date: Date) {
  return format(date, 'yyyy.MM.dd')
}

function formatTime(date: Date) {
  return format(date, 'MM/dd HH:mm')
}

function notificationTarget(groupId: string) {
  groupStore.selectGroup(groupId)
  router.push('/app/projects')
}

async function refreshSubtasks(taskId: string) {
  const latest = await taskService.getSubtasks(taskId)
  subtasks.value = { ...subtasks.value, [taskId]: latest }
}

function isSubtaskLoading(taskId: string, subtaskId: string) {
  const key = `${taskId}_${subtaskId}`
  return Boolean(subtaskLoading.value[key])
}

async function onSubtaskToggle(taskId: string, subtask: Subtask, event: Event) {
  const checkbox = event.target as HTMLInputElement
  const nextValue = checkbox.checked
  await toggleSubtask(taskId, subtask, nextValue, checkbox)
}

async function toggleSubtask(
  taskId: string,
  subtask: Subtask,
  isDone: boolean,
  control?: HTMLInputElement
) {
  const key = `${taskId}_${subtask.id}`
  subtaskLoading.value = { ...subtaskLoading.value, [key]: true }
  try {
    await taskService.toggleSubtaskDone({
      taskId,
      subtaskId: subtask.id,
      isDone
    })
    await refreshSubtasks(taskId)
  } catch (error) {
    console.error('Failed to toggle subtask', error)
    if (control) control.checked = !isDone
  } finally {
    const next = { ...subtaskLoading.value }
    delete next[key]
    subtaskLoading.value = next
  }
}

watch(
  () => [groupStore.currentGroupId, groupStore.groups.map((g) => g.id).join(',')],
  () => loadDashboard(),
  { immediate: true }
)
</script>

<style scoped>
.dashboard-grid {
  display: grid;
  gap: 24px;
}

@media (min-width: 1200px) {
  .dashboard-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .dashboard-grid > section:nth-child(3) {
    grid-column: span 2;
  }
}

.section-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.calendar-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.calendar-day {
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 18px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.02);
}

.calendar-day__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.calendar-day__date {
  font-weight: 600;
  letter-spacing: 0.02em;
}

.calendar-task {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.calendar-task:last-child {
  border-bottom: none;
}

.calendar-task__title {
  font-weight: 600;
}

.calendar-task__meta {
  color: var(--app-text-muted);
  font-size: 13px;
}

.task-card {
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 18px;
  padding: 18px;
  background: rgba(255, 255, 255, 0.02);
}

.task-card header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.task-card h4 {
  margin: 0 0 4px;
}

.task-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}

.desc {
  margin: 12px 0;
}

.subtasks {
  margin-top: 12px;
}

.subtask {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  gap: 12px;
}

.subtask__row {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.subtask__row input {
  accent-color: var(--app-primary-strong);
}

.notifications {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 18px;
  padding: 16px;
  gap: 12px;
  width: 100%;
  background: rgba(255, 255, 255, 0.01);
  color: inherit;
  cursor: pointer;
  text-align: left;
  transition: transform 0.2s ease, border 0.2s ease;
}

.notification:hover,
.notification:focus-visible {
  border-color: rgba(255, 255, 255, 0.35);
  transform: translateY(-1px);
}

.notification__title {
  font-weight: 600;
}

.notification__desc {
  color: var(--app-text-muted);
}

.notification__meta {
  font-size: 13px;
  color: var(--app-text-muted);
}

.notification__time {
  font-size: 13px;
  color: var(--app-text-muted);
  white-space: nowrap;
}
</style>
