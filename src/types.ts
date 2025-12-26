import type { DocumentData, QueryDocumentSnapshot } from 'firebase/firestore'

export type GroupRole = 'OWNER' | 'ADMIN' | 'MEMBER'

export interface Group {
  id: string
  name: string
  description?: string | null
  status: 'ACTIVE' | 'ARCHIVED'
  memberCount: number
  role: GroupRole
  invitationCode?: string | null
  createdAt?: Date | null
}

export interface GroupMember {
  id: string
  groupId: string
  userId: string
  email: string
  displayName: string
  role: GroupRole
  joinedAt?: Date | null
}

export interface Project {
  id: string
  groupId: string
  name: string
  description: string
  status: ProjectStatus
  createdBy: string
  createdAt?: Date | null
}

export type ProjectStatus = 'ACTIVE' | 'ARCHIVED'

export interface ProjectNote {
  id: string
  projectId: string
  title: string
  content: string
  createdBy: string
  createdAt?: Date | null
  updatedAt?: Date | null
}

export interface ProjectFile {
  id: string
  projectId: string
  fileName: string
  downloadUrl: string
  storagePath: string
  uploadedBy: string
  uploadedAt?: Date | null
  size: number
}

export type TaskStatus = 'TODO' | 'DOING' | 'DONE'
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH'

export interface Task {
  id: string
  projectId: string
  title: string
  description?: string | null
  assigneeId?: string | null
  dueDate?: Date | null
  status: TaskStatus
  priority: TaskPriority
  createdAt?: Date | null
  createdBy: string
}

export interface Subtask {
  id: string
  title: string
  isDone: boolean
  createdAt?: Date | null
}

export interface TaskComment {
  id: string
  userId: string
  content: string
  createdAt?: Date | null
}

export interface TaskActivity {
  id: string
  userId: string
  message: string
  createdAt?: Date | null
}

export interface ChatMessage {
  id: string
  senderId: string
  senderName: string
  content: string
  sentAt: Date
  channelType: MessageChannelType
  channelId: string
}

export type MessageChannelType = 'direct' | 'group'

export interface Friend {
  userId: string
  displayName: string
  handle: string
  photoUrl?: string | null
}

export interface UserProfile {
  uid: string
  userId: string
  tag: string
  displayName: string
  handle: string
  photoUrl?: string | null
}

export interface AppUser {
  uid: string
  displayName: string | null
  photoUrl: string | null
}

export interface FirebaseConverter<T> {
  fromFirestore(snapshot: QueryDocumentSnapshot<DocumentData>): T
  toFirestore(model: T): DocumentData
}

export function roleToString(role: GroupRole): string {
  return role
}

export function roleFromString(value?: string | null): GroupRole {
  if (value === 'OWNER' || value === 'ADMIN') return value
  return 'MEMBER'
}

export function projectStatusFromString(value?: string | null): ProjectStatus {
  if (value === 'ARCHIVED') return 'ARCHIVED'
  return 'ACTIVE'
}

export function taskStatusFromString(value?: string | null): TaskStatus {
  switch (value) {
    case 'DOING':
      return 'DOING'
    case 'DONE':
      return 'DONE'
    default:
      return 'TODO'
  }
}

export function taskPriorityFromString(value?: string | null): TaskPriority {
  switch (value) {
    case 'LOW':
      return 'LOW'
    case 'HIGH':
      return 'HIGH'
    default:
      return 'MEDIUM'
  }
}
