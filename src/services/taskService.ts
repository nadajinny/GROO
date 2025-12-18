import { apiRequest } from './apiClient'
import type {
  Subtask,
  Task,
  TaskActivity,
  TaskComment,
  TaskPriority,
  TaskStatus
} from '../types'

interface BackendTask {
  id: number
  projectId: number
  title: string
  description?: string | null
  assigneeId?: string | null
  status: TaskStatus
  priority: TaskPriority
  dueDate?: string | null
  createdBy: number
  createdAt?: string
  updatedAt?: string
}

interface BackendSubtask {
  id: number
  title: string
  done: boolean
  createdAt?: string
}

interface BackendComment {
  id: number
  authorId: number
  authorEmail?: string | null
  authorName?: string | null
  content: string
  createdAt?: string
}

interface BackendActivity {
  id: number
  userId: number
  userEmail?: string | null
  userName?: string | null
  message: string
  createdAt?: string
}

export async function createTask(params: {
  projectId: string
  title: string
  description?: string | null
  assigneeId?: string | null
  dueDate?: Date | null
  status?: TaskStatus
  priority?: TaskPriority
}): Promise<string> {
  const payload = {
    projectId: Number(params.projectId),
    title: params.title,
    description: params.description,
    assigneeId: params.assigneeId,
    dueDate: params.dueDate ? params.dueDate.toISOString() : null,
    status: params.status,
    priority: params.priority
  }
  const task = await apiRequest<BackendTask>('/api/tasks', { method: 'POST', body: payload })
  return task.id.toString()
}

export async function getTasksForProject(projectId: string): Promise<Task[]> {
  const data = await apiRequest<BackendTask[]>(`/api/tasks?projectId=${projectId}`)
  return data.map(mapTask)
}

export async function getTaskById(taskId: string): Promise<Task | null> {
  try {
    const task = await apiRequest<BackendTask>(`/api/tasks/${taskId}`)
    return mapTask(task)
  } catch (error) {
    console.error('Failed to load task', error)
    return null
  }
}

export async function updateTask(params: {
  taskId: string
  status?: TaskStatus
  priority?: TaskPriority
  assigneeId?: string | null
  updateAssignee?: boolean
  dueDate?: Date | null
  updateDueDate?: boolean
}): Promise<void> {
  const body: Record<string, unknown> = {}
  if (params.status) body.status = params.status
  if (params.priority) body.priority = params.priority
  if (params.updateAssignee) {
    body.updateAssignee = true
    body.assigneeId = params.assigneeId ?? null
  }
  if (params.updateDueDate) {
    body.updateDueDate = true
    body.dueDate = params.dueDate ? params.dueDate.toISOString() : null
  }
  await apiRequest(`/api/tasks/${params.taskId}`, { method: 'PATCH', body })
}

export async function addSubtask(params: { taskId: string; title: string }): Promise<void> {
  await apiRequest(`/api/tasks/${params.taskId}/subtasks`, {
    method: 'POST',
    body: { title: params.title }
  })
}

export async function getSubtasks(taskId: string): Promise<Subtask[]> {
  const data = await apiRequest<BackendSubtask[]>(`/api/tasks/${taskId}/subtasks`)
  return data.map((item) => ({
    id: item.id.toString(),
    title: item.title,
    isDone: item.done,
    createdAt: item.createdAt ? new Date(item.createdAt) : null
  }))
}

export async function toggleSubtaskDone(params: {
  taskId: string
  subtaskId: string
  isDone: boolean
}) {
  await apiRequest(`/api/tasks/${params.taskId}/subtasks/${params.subtaskId}?done=${params.isDone}`, {
    method: 'PATCH'
  })
}

export async function deleteSubtask(params: { taskId: string; subtaskId: string }) {
  await apiRequest(`/api/tasks/${params.taskId}/subtasks/${params.subtaskId}`, {
    method: 'DELETE'
  })
}

export async function getTaskComments(taskId: string): Promise<TaskComment[]> {
  const data = await apiRequest<BackendComment[]>(`/api/tasks/${taskId}/comments`)
  return data.map((item) => ({
    id: item.id.toString(),
    userId: item.authorName || item.authorEmail || item.authorId.toString(),
    content: item.content,
    createdAt: item.createdAt ? new Date(item.createdAt) : null
  }))
}

export async function addTaskComment(params: { taskId: string; userId: string; content: string }) {
  await apiRequest(`/api/tasks/${params.taskId}/comments`, {
    method: 'POST',
    body: { content: params.content }
  })
}

export async function getTaskActivityLogs(taskId: string): Promise<TaskActivity[]> {
  const data = await apiRequest<BackendActivity[]>(`/api/tasks/${taskId}/activity`)
  return data.map((item) => ({
    id: item.id.toString(),
    userId: item.userName || item.userEmail || item.userId.toString(),
    message: item.message,
    createdAt: item.createdAt ? new Date(item.createdAt) : null
  }))
}

function mapTask(task: BackendTask): Task {
  return {
    id: task.id.toString(),
    projectId: task.projectId.toString(),
    title: task.title,
    description: task.description ?? '',
    assigneeId: task.assigneeId ?? null,
    status: task.status,
    priority: task.priority,
    dueDate: task.dueDate ? new Date(task.dueDate) : null,
    createdBy: task.createdBy.toString(),
    createdAt: task.createdAt ? new Date(task.createdAt) : null
  }
}
