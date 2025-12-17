<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-header__title">{{ task?.title ?? '태스크 상세' }}</h2>
      <p class="page-header__subtitle">
        상태, 담당자, 서브태스크, 댓글, 활동 로그를 모두 확인할 수 있습니다.
      </p>
    </div>

    <section class="page-section app-card" v-if="isLoading">
      <div class="empty-state">태스크를 불러오는 중입니다...</div>
    </section>

    <section class="page-section app-card" v-else-if="!task">
      <div class="empty-state">태스크를 찾을 수 없습니다.</div>
    </section>

    <section class="page-section" v-else>
      <div class="task-detail-grid">
        <article class="app-card">
          <header class="task-header">
            <div>
              <h3>{{ task.title }}</h3>
              <p class="text-muted">작성자: {{ task.createdBy }}</p>
            </div>
            <div class="task-tags">
              <select class="select" v-model="taskStatus" @change="updateStatus">
                <option value="TODO">TODO</option>
                <option value="DOING">DOING</option>
                <option value="DONE">DONE</option>
              </select>
              <select class="select" v-model="taskPriority" @change="updatePriority">
                <option value="LOW">LOW</option>
                <option value="MEDIUM">MEDIUM</option>
                <option value="HIGH">HIGH</option>
              </select>
            </div>
          </header>
          <p class="text-muted">{{ task.description || '설명이 없습니다.' }}</p>
          <div class="task-meta">
            <div>
              <div class="text-muted">담당자</div>
              <input
                v-model="assignee"
                class="input"
                type="text"
                placeholder="핸들 또는 UID"
              />
            </div>
            <div>
              <div class="text-muted">마감일</div>
              <input v-model="dueDate" class="input" type="date" />
            </div>
          </div>
          <div class="card-actions">
            <button class="btn btn-primary" @click="saveAssignee">담당자 저장</button>
            <button class="btn btn-muted" @click="saveDueDate">마감일 저장</button>
          </div>
          <div v-if="taskUpdateError" class="error-banner">{{ taskUpdateError }}</div>
        </article>

        <article class="app-card">
          <h4>서브태스크</h4>
          <div class="card-actions">
            <input v-model="newSubtask" class="input" placeholder="서브태스크 제목" />
            <button class="btn btn-primary" @click="addSubtask">추가</button>
          </div>
          <ul class="list-reset subtask-list">
            <li v-for="sub in subtasks" :key="sub.id" class="subtask-item">
              <label>
                <input
                  type="checkbox"
                  :checked="sub.isDone"
                  @change="onSubtaskToggle(sub, $event)"
                />
                {{ sub.title }}
              </label>
              <button class="btn btn-outline" @click="deleteSubtask(sub)">삭제</button>
            </li>
          </ul>
        </article>

        <article class="app-card">
          <h4>댓글</h4>
          <div class="card-actions">
            <textarea v-model="commentInput" class="textarea" rows="3" placeholder="댓글 작성" />
            <button class="btn btn-primary" @click="addComment">등록</button>
          </div>
          <ul class="list-reset comment-list">
            <li v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-meta">
                <strong>{{ comment.userId }}</strong>
                <span class="text-muted">
                  {{ comment.createdAt ? formatDateTime(comment.createdAt) : '' }}
                </span>
              </div>
              <p>{{ comment.content }}</p>
            </li>
          </ul>
        </article>

        <article class="app-card">
          <h4>활동 로그</h4>
          <ul class="list-reset activity-list">
            <li v-for="log in activity" :key="log.id" class="activity-item">
              <div class="text-muted">{{ log.userId }}</div>
              <div>{{ log.message }}</div>
              <div class="text-muted">
                {{ log.createdAt ? formatDateTime(log.createdAt) : '' }}
              </div>
            </li>
          </ul>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'

import { useAuthStore } from '../stores/auth'
import * as taskService from '../services/taskService'
import * as userDirectory from '../services/userDirectory'
import type { Subtask, Task, TaskActivity, TaskComment } from '../types'

const route = useRoute()
const authStore = useAuthStore()

const task = ref<Task | null>(null)
const isLoading = ref(true)
const taskStatus = ref('TODO')
const taskPriority = ref('MEDIUM')
const assignee = ref('')
const dueDate = ref('')
const newSubtask = ref('')
const commentInput = ref('')
const subtasks = ref<Subtask[]>([])
const comments = ref<TaskComment[]>([])
const activity = ref<TaskActivity[]>([])
const taskUpdateError = ref<string | null>(null)

const taskId = computed(() => route.params.taskId as string)

loadTask()

async function loadTask() {
  isLoading.value = true
  try {
    const base = await taskService.getTaskById(taskId.value)
    if (!base) {
      task.value = null
      return
    }
    task.value = base
    taskStatus.value = base.status
    taskPriority.value = base.priority
    assignee.value = base.assigneeId ?? ''
    dueDate.value = base.dueDate ? formatInputDate(base.dueDate) : ''
    await Promise.all([loadSubtasks(), loadComments(), loadActivity()])
  } catch (error) {
    console.error('Failed to load task', error)
  } finally {
    isLoading.value = false
  }
}

async function loadSubtasks() {
  subtasks.value = await taskService.getSubtasks(taskId.value)
}

async function loadComments() {
  comments.value = await taskService.getTaskComments(taskId.value)
}

async function loadActivity() {
  activity.value = await taskService.getTaskActivityLogs(taskId.value)
}

async function updateStatus() {
  if (!task.value) return
  try {
    await taskService.updateTask({ taskId: task.value.id, status: taskStatus.value as any })
    task.value.status = taskStatus.value as any
    await loadActivity()
  } catch (error) {
    taskUpdateError.value = '상태를 변경하지 못했습니다.'
  }
}

async function updatePriority() {
  if (!task.value) return
  try {
    await taskService.updateTask({
      taskId: task.value.id,
      priority: taskPriority.value as any
    })
    task.value.priority = taskPriority.value as any
    await loadActivity()
  } catch (error) {
    taskUpdateError.value = '우선순위를 변경하지 못했습니다.'
  }
}

async function saveAssignee() {
  if (!task.value) return
  taskUpdateError.value = null
  try {
    let targetId = assignee.value.trim()
    if (!targetId) {
      await taskService.updateTask({
        taskId: task.value.id,
        updateAssignee: true,
        assigneeId: ''
      })
      task.value.assigneeId = null
      await loadActivity()
      return
    }
    const profile = await userDirectory.fetchByHandle(targetId)
    if (profile) {
      targetId = profile.uid
    }
    await taskService.updateTask({
      taskId: task.value.id,
      updateAssignee: true,
      assigneeId: targetId
    })
    task.value.assigneeId = targetId
    await loadActivity()
  } catch (error) {
    taskUpdateError.value = '담당자를 저장하지 못했습니다.'
  }
}

async function saveDueDate() {
  if (!task.value) return
  try {
    await taskService.updateTask({
      taskId: task.value.id,
      updateDueDate: true,
      dueDate: dueDate.value ? new Date(dueDate.value) : null
    })
    task.value.dueDate = dueDate.value ? new Date(dueDate.value) : null
    await loadActivity()
  } catch (error) {
    taskUpdateError.value = '마감일을 저장하지 못했습니다.'
  }
}

async function addSubtask() {
  if (!newSubtask.value.trim()) return
  try {
    await taskService.addSubtask({ taskId: taskId.value, title: newSubtask.value })
    newSubtask.value = ''
    await loadSubtasks()
  } catch (error) {
    taskUpdateError.value = '서브태스크 추가 실패'
  }
}

function onSubtaskToggle(subtask: Subtask, event: Event) {
  const input = event.target as HTMLInputElement
  toggleSubtask(subtask, input.checked)
}

async function toggleSubtask(subtask: Subtask, checked: boolean) {
  await taskService.toggleSubtaskDone({
    taskId: taskId.value,
    subtaskId: subtask.id,
    isDone: checked
  })
  await loadSubtasks()
}

async function deleteSubtask(subtask: Subtask) {
  await taskService.deleteSubtask({ taskId: taskId.value, subtaskId: subtask.id })
  await loadSubtasks()
}

async function addComment() {
  if (!commentInput.value.trim()) return
  const userId = authStore.user?.uid
  if (!userId) {
    taskUpdateError.value = '로그인 후 이용해주세요.'
    return
  }
  await taskService.addTaskComment({
    taskId: taskId.value,
    userId,
    content: commentInput.value
  })
  commentInput.value = ''
  await loadComments()
  await loadActivity()
}

function formatInputDate(date: Date) {
  const pad = (value: number) => `${value}`.padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

function formatDateTime(date: Date) {
  const pad = (value: number) => `${value}`.padStart(2, '0')
  return `${date.getFullYear()}.${pad(date.getMonth() + 1)}.${pad(date.getDate())} ${pad(
    date.getHours()
  )}:${pad(date.getMinutes())}`
}
</script>

<style scoped>
.task-detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 20px;
}

.task-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.task-tags {
  display: flex;
  gap: 10px;
}

.task-meta {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.subtask-list,
.comment-list,
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 12px;
}

.subtask-item,
.comment-item,
.activity-item {
  border: 1px solid var(--app-border);
  border-radius: 14px;
  padding: 12px;
}

.comment-meta {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--app-text-muted);
}

.error-banner {
  margin-top: 16px;
  border-radius: 12px;
  padding: 12px;
  background: rgba(255, 107, 107, 0.15);
  border: 1px solid rgba(255, 107, 107, 0.4);
}
</style>
