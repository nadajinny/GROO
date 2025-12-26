import { apiRequest } from './apiClient'

export interface BackendGroupSummary {
  id: number
  name: string
  description?: string | null
  status: 'ACTIVE' | 'ARCHIVED'
  myRole: GroupRole
  memberCount: number
  createdAt?: string
}

export type GroupRole = 'OWNER' | 'ADMIN' | 'MEMBER'

export interface BackendGroupDetail extends BackendGroupSummary {
  invitationCode: string
  updatedAt?: string
  members: BackendGroupMember[]
}

export interface BackendGroupMember {
  membershipId: number
  userId: number
  email: string
  displayName: string
  role: GroupRole
  joinedAt?: string
}

export async function fetchMyGroups() {
  return apiRequest<BackendGroupSummary[]>('/api/groups')
}

export async function getGroup(groupId: number | string) {
  return apiRequest<BackendGroupDetail>(`/api/groups/${groupId}`)
}

export async function createGroup(payload: { name: string; description?: string }) {
  return apiRequest<BackendGroupDetail>('/api/groups', {
    method: 'POST',
    body: payload
  })
}

export async function updateGroup(
  groupId: number | string,
  payload: { name: string; description?: string; archived?: boolean }
) {
  return apiRequest<BackendGroupDetail>(`/api/groups/${groupId}`, {
    method: 'PUT',
    body: payload
  })
}

export async function regenerateInvitation(groupId: number | string) {
  return apiRequest<string>(`/api/groups/${groupId}/invites`, {
    method: 'POST'
  })
}

export async function joinWithCode(payload: { invitationCode: string }) {
  return apiRequest<BackendGroupDetail>('/api/groups/join', {
    method: 'POST',
    body: payload
  })
}

export async function listMembers(groupId: number | string) {
  return apiRequest<BackendGroupMember[]>(`/api/groups/${groupId}/members`)
}

export async function addMember(
  groupId: number | string,
  payload: { email: string; role?: GroupRole }
) {
  return apiRequest<BackendGroupMember>(`/api/groups/${groupId}/members`, {
    method: 'POST',
    body: payload
  })
}

export async function removeMember(groupId: number | string, membershipId: number | string) {
  return apiRequest<void>(`/api/groups/${groupId}/members/${membershipId}`, {
    method: 'DELETE'
  })
}
