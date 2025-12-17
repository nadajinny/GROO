import {
  Timestamp,
  addDoc,
  collection,
  deleteDoc,
  doc,
  getDoc,
  getDocs,
  limit,
  orderBy,
  query,
  serverTimestamp,
  updateDoc,
  where
} from 'firebase/firestore'

import { firestore } from '../firebase'
import type {
  Subtask,
  Task,
  TaskActivity,
  TaskComment,
  TaskPriority,
  TaskStatus
} from '../types'
import { taskPriorityFromString, taskStatusFromString } from '../types'

const tasksCollection = collection(firestore, 'tasks')

export async function createTask(params: {
  projectId: string
  title: string
  description?: string | null
  assigneeId?: string | null
  dueDate?: Date | null
  status?: TaskStatus
  priority?: TaskPriority
  currentUserId: string
}): Promise<string> {
  const trimmedTitle = params.title.trim()
  if (!trimmedTitle) throw new Error('태스크 제목을 입력해주세요.')
  const docRef = await addDoc(tasksCollection, {
    projectId: params.projectId,
    title: trimmedTitle,
    description: params.description?.trim() ?? '',
    assigneeId: params.assigneeId?.trim() || null,
    dueDate: params.dueDate ? Timestamp.fromDate(params.dueDate) : null,
    status: params.status ?? 'TODO',
    priority: params.priority ?? 'MEDIUM',
    createdBy: params.currentUserId,
    createdAt: serverTimestamp()
  })
  return docRef.id
}

export async function getTasksForProject(projectId: string): Promise<Task[]> {
  const q = query(tasksCollection, where('projectId', '==', projectId), orderBy('dueDate', 'asc'))
  const snapshot = await getDocs(q)
  return snapshot.docs.map(mapTask)
}

export async function getTaskById(taskId: string): Promise<Task | null> {
  const snapshot = await getDoc(doc(tasksCollection, taskId))
  if (!snapshot.exists()) return null
  return mapTaskSnapshot(snapshot)
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
  const docRef = doc(tasksCollection, params.taskId)
  const updateData: Record<string, unknown> = {}
  if (params.status) updateData.status = params.status
  if (params.priority) updateData.priority = params.priority
  if (params.updateAssignee) {
    const value = params.assigneeId?.trim() ?? ''
    updateData.assigneeId = value || null
  }
  if (params.updateDueDate) {
    updateData.dueDate = params.dueDate ? Timestamp.fromDate(params.dueDate) : null
  }
  if (!Object.keys(updateData).length) return
  updateData.updatedAt = serverTimestamp()
  await updateDoc(docRef, updateData)
}

export async function addSubtask(params: { taskId: string; title: string }): Promise<string> {
  const trimmed = params.title.trim()
  if (!trimmed) throw new Error('서브태스크 제목을 입력해주세요.')
  const subtasksRef = collection(tasksCollection, params.taskId, 'subtasks')
  const docRef = await addDoc(subtasksRef, {
    title: trimmed,
    isDone: false,
    createdAt: serverTimestamp()
  })
  return docRef.id
}

export async function getSubtasks(taskId: string): Promise<Subtask[]> {
  const snapshot = await getDocs(
    query(collection(tasksCollection, taskId, 'subtasks'), orderBy('createdAt'))
  )
  return snapshot.docs.map((doc) => ({
    id: doc.id,
    title: doc.data().title ?? '',
    isDone: Boolean(doc.data().isDone),
    createdAt: doc.data().createdAt ? doc.data().createdAt.toDate() : null
  }))
}

export async function toggleSubtaskDone(params: {
  taskId: string
  subtaskId: string
  isDone: boolean
}) {
  await updateDoc(doc(tasksCollection, params.taskId, 'subtasks', params.subtaskId), {
    isDone: params.isDone
  })
}

export async function deleteSubtask(params: { taskId: string; subtaskId: string }) {
  await deleteDoc(doc(tasksCollection, params.taskId, 'subtasks', params.subtaskId))
}

export async function getTaskComments(taskId: string): Promise<TaskComment[]> {
  const snapshot = await getDocs(
    query(collection(tasksCollection, taskId, 'comments'), orderBy('createdAt'))
  )
  return snapshot.docs.map((doc) => ({
    id: doc.id,
    userId: doc.data().userId ?? 'unknown',
    content: doc.data().content ?? '',
    createdAt: doc.data().createdAt ? doc.data().createdAt.toDate() : null
  }))
}

export async function addTaskComment(params: {
  taskId: string
  userId: string
  content: string
}): Promise<string> {
  const trimmed = params.content.trim()
  if (!trimmed) throw new Error('댓글 내용을 입력해주세요.')
  const docRef = await addDoc(collection(tasksCollection, params.taskId, 'comments'), {
    userId: params.userId,
    content: trimmed,
    createdAt: serverTimestamp()
  })
  return docRef.id
}

export async function getTaskActivityLogs(taskId: string): Promise<TaskActivity[]> {
  const snapshot = await getDocs(
    query(
      collection(tasksCollection, taskId, 'activity'),
      orderBy('createdAt', 'desc'),
      limit(50)
    )
  )
  return snapshot.docs.map((doc) => ({
    id: doc.id,
    userId: doc.data().userId ?? 'unknown',
    message: doc.data().message ?? '',
    createdAt: doc.data().createdAt ? doc.data().createdAt.toDate() : null
  }))
}

export async function addTaskActivityLog(params: {
  taskId: string
  userId: string
  message: string
}) {
  const trimmed = params.message.trim()
  if (!trimmed) return
  await addDoc(collection(tasksCollection, params.taskId, 'activity'), {
    userId: params.userId,
    message: trimmed,
    createdAt: serverTimestamp()
  })
}

function mapTask(doc: any): Task {
  const data = doc.data()
  return {
    id: doc.id,
    projectId: data.projectId ?? '',
    title: data.title ?? '제목 없음',
    description: data.description ?? '',
    assigneeId: data.assigneeId ?? null,
    dueDate: data.dueDate ? data.dueDate.toDate() : null,
    status: taskStatusFromString(data.status),
    priority: taskPriorityFromString(data.priority),
    createdAt: data.createdAt ? data.createdAt.toDate() : null,
    createdBy: data.createdBy ?? ''
  }
}

function mapTaskSnapshot(docSnap: any): Task {
  return mapTask(docSnap)
}
