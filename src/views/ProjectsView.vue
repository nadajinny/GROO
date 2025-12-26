<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-header__title">프로젝트</h2>
      <p class="page-header__subtitle">
        현재 선택된 그룹 기준으로 프로젝트를 생성하고 태스크를 관리합니다.
      </p>
    </div>

    <section class="page-section">
      <CurrentWorkspaceBanner :use-card="false" />
    </section>

    <section class="page-section app-card" v-if="!groupStore.groups.length">
      <div class="empty-state">
        아직 참여 중인 그룹이 없습니다. 먼저 그룹을 생성하거나 초대를 받아 프로젝트를 만들어보세요.
      </div>
    </section>

    <section class="page-section app-card" v-else-if="!groupStore.currentGroup">
      <div class="empty-state">
        현재 작업 그룹을 선택하면 프로젝트 목록이 표시됩니다.
      </div>
    </section>

    <section class="page-section" v-else>
      <div class="projects-layout">
        <aside class="projects-sidebar">
          <div class="sidebar-actions">
            <button class="btn btn-primary" :disabled="isFetchingProjects" @click="openCreateProjectDialog">
              새 프로젝트
            </button>
            <button class="btn btn-outline" :disabled="isFetchingProjects" @click="loadProjects">
              새로고침
            </button>
          </div>
          <div v-if="isFetchingProjects && !projects.length" class="empty-state">
            프로젝트를 불러오는 중입니다...
          </div>
          <ul v-else class="list-reset project-list">
            <li
              v-for="project in projects"
              :key="project.id"
              :class="['project-list__item', { active: project.id === selectedProjectId }]"
              @click="selectProject(project.id)"
            >
              <div class="project-list__title">{{ project.name }}</div>
              <div class="project-list__desc">{{ project.description }}</div>
              <span class="chip">{{ project.status }}</span>
            </li>
          </ul>
          <div v-if="!projects.length && !isFetchingProjects" class="empty-state">
            아직 프로젝트가 없습니다. 새 프로젝트를 생성해보세요.
          </div>
        </aside>
        <Transition name="panel-fade" mode="out-in">
        <div
          v-if="selectedProject"
          :key="selectedProject.id"
          class="projects-content"
        >
          <header class="projects-content__header">
            <div>
              <h3>{{ selectedProject.name }}</h3>
              <p class="text-muted">{{ selectedProject.description || '설명이 없습니다.' }}</p>
            </div>
            <div class="card-actions">
              <button class="btn btn-primary" :disabled="isLoadingTasks" @click="openCreateTaskDialog">
                새 태스크
              </button>
              <button class="btn btn-outline" :disabled="isLoadingTasks" @click="loadTasks">
                태스크 새로고침
              </button>
            </div>
          </header>

          <section class="projects-section">
            <div class="section-header">
              <div>
                <h4>프로젝트 노트</h4>
                <p class="text-muted">간단한 회의 메모나 결정 사항을 기록하세요.</p>
              </div>
              <button class="btn btn-muted" :disabled="isLoadingNotes" @click="openCreateNoteDialog">
                노트 추가
              </button>
            </div>
            <div v-if="isLoadingNotes" class="empty-state">노트를 불러오는 중입니다...</div>
            <div v-else-if="notesError" class="empty-state">{{ notesError }}</div>
            <div v-else-if="!notes.length" class="empty-state">등록된 노트가 없습니다.</div>
            <ul v-else class="list-reset card-list">
              <li v-for="note in notes" :key="note.id" class="card-with-border note-card">
                <header>
                  <strong>{{ note.title }}</strong>
                  <button class="btn btn-outline" @click="deleteNote(note.id)">삭제</button>
                </header>
                <p class="text-muted">{{ note.content }}</p>
              </li>
            </ul>
          </section>

          <section class="projects-section">
            <div class="section-header">
              <div>
                <h4>프로젝트 파일</h4>
                <p class="text-muted">자료나 산출물을 업로드하여 공유하세요. (최대 15MB)</p>
              </div>
              <label class="btn btn-muted upload-btn">
                파일 업로드
                <input type="file" hidden @change="handleFileSelected" />
              </label>
            </div>
            <div v-if="isLoadingFiles" class="empty-state">파일을 불러오는 중입니다...</div>
            <div v-else-if="filesError" class="empty-state">{{ filesError }}</div>
            <div v-else-if="!files.length" class="empty-state">등록된 파일이 없습니다.</div>
            <ul v-else class="list-reset card-list">
              <li v-for="file in files" :key="file.id" class="card-with-border file-card">
                <div>
                  <a :href="file.downloadUrl" target="_blank" rel="noreferrer">{{ file.fileName }}</a>
                  <div class="text-muted">
                    {{ formatDateTime(file.uploadedAt ?? new Date()) }} · {{ formatBytes(file.size) }}
                  </div>
                </div>
                <button class="btn btn-outline" @click="deleteFile(file.id)">삭제</button>
              </li>
            </ul>
          </section>

          <section class="projects-section">
            <div class="section-header">
              <div>
                <h4>태스크</h4>
                <p class="text-muted">필터를 적용하거나 정렬을 변경할 수 있습니다.</p>
              </div>
              <button class="btn btn-muted" @click="resetFilters">필터 초기화</button>
            </div>
            <div class="filters">
              <select class="select" v-model="filterState.status">
                <option value="">전체 상태</option>
                <option value="TODO">TODO</option>
                <option value="DOING">DOING</option>
                <option value="DONE">DONE</option>
              </select>
              <select class="select" v-model="filterState.priority">
                <option value="">전체 우선순위</option>
                <option value="LOW">LOW</option>
                <option value="MEDIUM">MEDIUM</option>
                <option value="HIGH">HIGH</option>
              </select>
              <select class="select" v-model="filterState.dueFilter">
                <option value="all">모든 마감일</option>
                <option value="noDueDate">마감일 없음</option>
                <option value="hasDueDate">마감일 있음</option>
                <option value="overdue">기한 경과</option>
                <option value="dueNext7Days">7일 이내</option>
              </select>
              <input
                class="input"
                v-model="filterState.assigneeQuery"
                type="text"
                placeholder="담당자 검색 (UID/핸들)"
              />
              <select class="select" v-model="sortField">
                <option value="title">제목</option>
                <option value="status">상태</option>
                <option value="priority">우선순위</option>
                <option value="assignee">담당자</option>
                <option value="dueDate">마감일</option>
              </select>
              <button class="btn btn-outline" @click="toggleSortDirection">
                {{ isSortAscending ? '오름차순' : '내림차순' }}
              </button>
            </div>
            <div v-if="isLoadingTasks && !tasks.length" class="empty-state">
              태스크를 불러오는 중입니다...
            </div>
            <div v-else-if="taskError" class="empty-state">{{ taskError }}</div>
            <div v-else-if="!visibleTasks.length" class="empty-state">
              조건에 맞는 태스크가 없습니다.
            </div>
            <table v-else class="table">
              <thead>
                <tr>
                  <th>제목</th>
                  <th>상태</th>
                  <th>우선순위</th>
                  <th>담당자</th>
                  <th>마감일</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="task in visibleTasks"
                  :key="task.id"
                  @click="openTaskDetail(task.id)"
                  class="clickable-row"
                >
                  <td>{{ task.title }}</td>
                  <td>{{ task.status }}</td>
                  <td>{{ task.priority }}</td>
                  <td>{{ task.assigneeId ?? '-' }}</td>
                  <td>{{ task.dueDate ? formatDate(task.dueDate) : '-' }}</td>
                </tr>
              </tbody>
            </table>
          </section>
        </div>
        </Transition>
      </div>
    </section>

    <AppDialog v-model="createProjectDialog" title="새 프로젝트">
      <input v-model="projectForm.name" class="input" type="text" placeholder="프로젝트 이름" />
      <textarea
        v-model="projectForm.description"
        class="textarea"
        rows="3"
        placeholder="설명"
      />
      <template #footer>
        <button class="btn btn-outline" @click="closeCreateProjectDialog">취소</button>
        <button class="btn btn-primary" @click="submitCreateProject">생성</button>
      </template>
    </AppDialog>

    <AppDialog v-model="createTaskDialog" title="새 태스크">
      <input v-model="taskForm.title" class="input" type="text" placeholder="태스크 제목" />
      <textarea
        v-model="taskForm.description"
        class="textarea"
        rows="3"
        placeholder="설명 (선택)"
      />
      <input
        v-model="taskForm.assignee"
        class="input"
        type="text"
        placeholder="담당자 UID 또는 핸들"
      />
      <template #footer>
        <button class="btn btn-outline" @click="closeCreateTaskDialog">취소</button>
        <button class="btn btn-primary" @click="submitCreateTask">생성</button>
      </template>
    </AppDialog>

    <AppDialog v-model="createNoteDialog" title="프로젝트 노트">
      <input v-model="noteForm.title" class="input" placeholder="노트 제목" />
      <textarea v-model="noteForm.content" class="textarea" rows="4" placeholder="내용" />
      <template #footer>
        <button class="btn btn-outline" @click="closeCreateNoteDialog">취소</button>
        <button class="btn btn-primary" @click="submitCreateNote">저장</button>
      </template>
    </AppDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import CurrentWorkspaceBanner from '../components/groups/CurrentWorkspaceBanner.vue'
import AppDialog from '../components/common/AppDialog.vue'
import { useAuthStore } from '../stores/auth'
import { useGroupStore } from '../stores/group'
import * as projectService from '../services/projectService'
import * as taskService from '../services/taskService'
import * as projectNoteService from '../services/projectNoteService'
import * as projectFileService from '../services/projectFileService'
import type { Project, ProjectFile, ProjectNote, Task } from '../types'

const authStore = useAuthStore()
const groupStore = useGroupStore()
const router = useRouter()

const projects = ref<Project[]>([])
const selectedProjectId = ref<string | null>(null)
const tasks = ref<Task[]>([])
const notes = ref<ProjectNote[]>([])
const files = ref<ProjectFile[]>([])
const isFetchingProjects = ref(false)
const isLoadingTasks = ref(false)
const isLoadingNotes = ref(false)
const isLoadingFiles = ref(false)
const taskError = ref<string | null>(null)
const notesError = ref<string | null>(null)
const filesError = ref<string | null>(null)
const filterState = reactive({
  status: '',
  priority: '',
  dueFilter: 'all',
  assigneeQuery: ''
})
const sortField = ref<'title' | 'status' | 'priority' | 'assignee' | 'dueDate'>('dueDate')
const isSortAscending = ref(true)

const createProjectDialog = ref(false)
const createTaskDialog = ref(false)
const createNoteDialog = ref(false)
const projectForm = reactive({ name: '', description: '' })
const taskForm = reactive({ title: '', description: '', assignee: '' })
const noteForm = reactive({ title: '', content: '' })

const selectedProject = computed(() =>
  projects.value.find((project) => project.id === selectedProjectId.value) ?? null
)

const visibleTasks = computed(() => applyTaskFilters(tasks.value))

watch(
  () => groupStore.currentGroupId,
  () => {
    selectedProjectId.value = null
    projects.value = []
    tasks.value = []
    if (groupStore.currentGroupId) {
      loadProjects()
    }
  },
  { immediate: true }
)

async function loadProjects() {
  const group = groupStore.currentGroup
  if (!group) return
  isFetchingProjects.value = true
  try {
    projects.value = await projectService.getProjectsForGroup(group.id)
    if (projects.value.length) {
      const firstProject = projects.value[0]
      if (firstProject) {
        selectedProjectId.value = firstProject.id
        await Promise.all([loadTasks(), loadNotes(), loadFiles()])
      }
    } else {
      selectedProjectId.value = null
      tasks.value = []
      notes.value = []
      files.value = []
    }
  } catch (error) {
    console.error('Failed to fetch projects', error)
  } finally {
    isFetchingProjects.value = false
  }
}

function selectProject(id: string) {
  if (selectedProjectId.value === id) return
  selectedProjectId.value = id
  loadTasks()
  loadNotes()
  loadFiles()
}

async function loadTasks() {
  const id = selectedProjectId.value
  if (!id) return
  isLoadingTasks.value = true
  taskError.value = null
  try {
    tasks.value = await taskService.getTasksForProject(id)
  } catch (error) {
    console.error('Failed to fetch tasks', error)
    taskError.value = '태스크 목록을 불러오지 못했습니다.'
  } finally {
    isLoadingTasks.value = false
  }
}

async function loadNotes() {
  const id = selectedProjectId.value
  if (!id) return
  isLoadingNotes.value = true
  notesError.value = null
  try {
    notes.value = await projectNoteService.getProjectNotes(id)
  } catch (error) {
    notesError.value = '프로젝트 노트를 불러오지 못했습니다.'
  } finally {
    isLoadingNotes.value = false
  }
}

async function loadFiles() {
  const id = selectedProjectId.value
  if (!id) return
  isLoadingFiles.value = true
  filesError.value = null
  try {
    files.value = await projectFileService.getProjectFiles(id)
  } catch (error) {
    filesError.value = '프로젝트 파일을 불러오지 못했습니다.'
  } finally {
    isLoadingFiles.value = false
  }
}

function openCreateProjectDialog() {
  createProjectDialog.value = true
  projectForm.name = ''
  projectForm.description = ''
}

function closeCreateProjectDialog() {
  createProjectDialog.value = false
}

async function submitCreateProject() {
  const group = groupStore.currentGroup
  if (!group || !authStore.user) return
  try {
    await projectService.createProject({
      groupId: group.id,
      name: projectForm.name,
      description: projectForm.description
    })
    closeCreateProjectDialog()
    await loadProjects()
  } catch (error) {
    console.error('Failed to create project', error)
  }
}

function openCreateTaskDialog() {
  createTaskDialog.value = true
  taskForm.title = ''
  taskForm.description = ''
  taskForm.assignee = ''
}

function closeCreateTaskDialog() {
  createTaskDialog.value = false
}

async function submitCreateTask() {
  if (!authStore.user || !selectedProjectId.value) return
  try {
    await taskService.createTask({
      projectId: selectedProjectId.value,
      title: taskForm.title,
      description: taskForm.description,
      assigneeId: taskForm.assignee
    })
    closeCreateTaskDialog()
    await loadTasks()
  } catch (error) {
    console.error('Failed to create task', error)
  }
}

function openCreateNoteDialog() {
  createNoteDialog.value = true
  noteForm.title = ''
  noteForm.content = ''
}

function closeCreateNoteDialog() {
  createNoteDialog.value = false
}

async function submitCreateNote() {
  const userId = authStore.user?.uid
  if (!userId || !selectedProjectId.value) return
  try {
    await projectNoteService.addProjectNote({
      projectId: selectedProjectId.value,
      title: noteForm.title,
      content: noteForm.content,
      userId
    })
    closeCreateNoteDialog()
    await loadNotes()
  } catch (error) {
    console.error('Failed to add note', error)
  }
}

async function deleteNote(noteId: string) {
  if (!selectedProjectId.value) return
  await projectNoteService.deleteProjectNote({
    projectId: selectedProjectId.value,
    noteId
  })
  await loadNotes()
}

async function handleFileSelected(event: Event) {
  const projectId = selectedProjectId.value
  const userId = authStore.user?.uid
  if (!projectId || !userId) return
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (file.size > 15 * 1024 * 1024) {
    filesError.value = '최대 15MB 이하의 파일만 업로드할 수 있습니다.'
    return
  }
  try {
    const buffer = await file.arrayBuffer()
    await projectFileService.uploadProjectFile({
      projectId,
      fileName: file.name,
      data: buffer,
      contentType: file.type || 'application/octet-stream',
      userId
    })
    await loadFiles()
  } catch (error) {
    console.error('Failed to upload file', error)
    filesError.value = '파일 업로드에 실패했습니다.'
  } finally {
    ;(event.target as HTMLInputElement).value = ''
  }
}

async function deleteFile(fileId: string) {
  const projectId = selectedProjectId.value
  if (!projectId) return
  const file = files.value.find((item) => item.id === fileId)
  if (!file) return
  await projectFileService.deleteProjectFile({ projectId, file })
  await loadFiles()
}

function resetFilters() {
  filterState.status = ''
  filterState.priority = ''
  filterState.dueFilter = 'all'
  filterState.assigneeQuery = ''
  sortField.value = 'dueDate'
  isSortAscending.value = true
}

function toggleSortDirection() {
  isSortAscending.value = !isSortAscending.value
}

function applyTaskFilters(items: Task[]): Task[] {
  const filtered = items.filter((task) => {
    if (filterState.status && task.status !== filterState.status) return false
    if (filterState.priority && task.priority !== filterState.priority) return false
    const query = filterState.assigneeQuery.trim().toLowerCase()
    if (query && !(task.assigneeId ?? '').toLowerCase().includes(query)) return false
    switch (filterState.dueFilter) {
      case 'noDueDate':
        if (task.dueDate) return false
        break
      case 'hasDueDate':
        if (!task.dueDate) return false
        break
      case 'overdue':
        if (!task.dueDate || task.dueDate >= startOfToday()) return false
        break
      case 'dueNext7Days':
        if (!task.dueDate) return false
        if (task.dueDate < startOfToday()) return false
        if (task.dueDate > addDays(startOfToday(), 7)) return false
        break
    }
    return true
  })

  const sorted = filtered.sort((a, b) => compareTasks(a, b))
  return isSortAscending.value ? sorted : sorted.reverse()
}

function compareTasks(a: Task, b: Task): number {
  switch (sortField.value) {
    case 'title':
      return a.title.localeCompare(b.title)
    case 'status':
      return a.status.localeCompare(b.status)
    case 'priority':
      return a.priority.localeCompare(b.priority)
    case 'assignee':
      return (a.assigneeId ?? '').localeCompare(b.assigneeId ?? '')
    case 'dueDate':
      return (a.dueDate?.getTime() ?? Infinity) - (b.dueDate?.getTime() ?? Infinity)
  }
}

function startOfToday() {
  const now = new Date()
  return new Date(now.getFullYear(), now.getMonth(), now.getDate())
}

function addDays(date: Date, days: number) {
  const next = new Date(date)
  next.setDate(next.getDate() + days)
  return next
}

function formatDate(date: Date) {
  const pad = (value: number) => `${value}`.padStart(2, '0')
  return `${date.getFullYear()}.${pad(date.getMonth() + 1)}.${pad(date.getDate())}`
}

function formatDateTime(date: Date) {
  const pad = (value: number) => `${value}`.padStart(2, '0')
  return `${date.getFullYear()}.${pad(date.getMonth() + 1)}.${pad(date.getDate())} ${pad(
    date.getHours()
  )}:${pad(date.getMinutes())}`
}

function formatBytes(bytes: number) {
  if (bytes === 0) return '0B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${(bytes / Math.pow(k, i)).toFixed(1)}${sizes[i]}`
}

function openTaskDetail(taskId: string) {
  router.push(`/app/tasks/${taskId}`)
}
</script>

<style scoped>
.projects-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 20px;
}

@media (max-width: 1024px) {
  .projects-layout {
    grid-template-columns: 1fr;
  }
}

.projects-sidebar {
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius);
  padding: 18px;
  min-height: 200px;
}

.sidebar-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
}

.project-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 520px;
  overflow-y: auto;
}

.project-list__item {
  border: 1px solid var(--app-border);
  border-radius: 14px;
  padding: 12px;
  cursor: pointer;
}

.project-list__item.active {
  border-color: rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.04);
}

.project-list__title {
  font-weight: 600;
}

.project-list__desc {
  color: var(--app-text-muted);
  font-size: 13px;
}

.projects-content {
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.projects-content__header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.projects-section {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 16px;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 12px;
}

.note-card header,
.file-card {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.filters {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.clickable-row {
  cursor: pointer;
}

.clickable-row:hover {
  background: rgba(255, 255, 255, 0.05);
}

.upload-btn {
  cursor: pointer;
}

.panel-fade-enter-active,
.panel-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.panel-fade-enter-from,
.panel-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
