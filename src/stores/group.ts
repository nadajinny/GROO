import { defineStore } from 'pinia'
import { computed, ref, watch, type WatchStopHandle } from 'vue'

import type { Group, GroupMember } from '../types'
import type { useAuthStore } from './auth'
import * as groupApi from '../services/groupApi'
import type {
  BackendGroupDetail,
  BackendGroupSummary,
  BackendGroupMember
} from '../services/groupApi'

interface GroupDetailRecord {
  info: Group
  invitationCode: string
  members: GroupMember[]
}

export const useGroupStore = defineStore('groups', () => {
  const groups = ref<Group[]>([])
  const currentGroupId = ref<string | null>(null)
  const allowNullSelection = ref(true)
  const isFetching = ref(false)
  const isCreating = ref(false)
  const errorMessage = ref<string | null>(null)
  const membersFetching = ref<Set<string>>(new Set())
  const memberErrors = ref<Set<string>>(new Set())
  const memberOperations = ref<Set<string>>(new Set())
  const details = ref<Record<string, GroupDetailRecord>>({})

  let stopAuthWatch: WatchStopHandle | null = null

  function init(authStore: ReturnType<typeof useAuthStore>) {
    if (stopAuthWatch) return
    stopAuthWatch = watch(
      () => authStore.user?.uid ?? null,
      (uid) => {
        if (uid) {
          fetchMyGroups()
        } else {
          resetState()
        }
      },
      { immediate: true }
    )
  }

  function resetState() {
    groups.value = []
    currentGroupId.value = null
    allowNullSelection.value = true
    isFetching.value = false
    isCreating.value = false
    errorMessage.value = null
    membersFetching.value = new Set()
    memberErrors.value = new Set()
    memberOperations.value = new Set()
    details.value = {}
  }

  async function fetchMyGroups() {
    isFetching.value = true
    errorMessage.value = null
    try {
      const list = await groupApi.fetchMyGroups()
      groups.value = list.map(mapSummary)
      reconcileCurrentGroupSelection()
    } catch (error: any) {
      console.error('Failed to load groups', error)
      errorMessage.value = error?.message ?? '그룹 목록을 불러오지 못했습니다.'
    } finally {
      isFetching.value = false
    }
  }

  async function createGroup(name: string, description?: string) {
    if (isCreating.value) return null
    isCreating.value = true
    errorMessage.value = null
    try {
      const detail = await groupApi.createGroup({ name, description })
      const mapped = storeDetail(detail)
      groups.value = [mapped.info, ...groups.value]
      selectGroup(mapped.info.id)
      return mapped.info.id
    } catch (error: any) {
      console.error('Failed to create group', error)
      errorMessage.value = error?.message ?? '그룹 생성에 실패했습니다.'
      return null
    } finally {
      isCreating.value = false
    }
  }

  async function updateGroup(groupId: string, params: { name: string; description?: string }) {
    try {
      const detail = await groupApi.updateGroup(groupId, {
        name: params.name,
        description: params.description,
        archived: false
      })
      const mapped = storeDetail(detail)
      updateGroupInList(mapped.info)
      return mapped.info
    } catch (error: any) {
      console.error('Failed to update group', error)
      errorMessage.value = error?.message ?? '그룹 정보를 수정하지 못했습니다.'
      throw error
    }
  }

  async function joinGroupByCode(code: string) {
    try {
      const detail = await groupApi.joinWithCode({ invitationCode: code })
      const mapped = storeDetail(detail)
      const existing = groups.value.find((item) => item.id === mapped.info.id)
      if (existing) {
        updateGroupInList(mapped.info)
      } else {
        groups.value = [mapped.info, ...groups.value]
      }
      selectGroup(mapped.info.id)
      return mapped.info
    } catch (error: any) {
      console.error('Failed to join group', error)
      errorMessage.value = error?.message ?? '초대 코드로 그룹에 참여하지 못했습니다.'
      throw error
    }
  }

  async function regenerateInvitation(groupId: string) {
    try {
      const code = await groupApi.regenerateInvitation(groupId)
      const detail = details.value[groupId]
      if (detail) {
        details.value = {
          ...details.value,
          [groupId]: {
            ...detail,
            invitationCode: code
          }
        }
      }
      return code
    } catch (error: any) {
      console.error('Failed to regenerate invitation code', error)
      errorMessage.value = error?.message ?? '초대 코드를 재발급하지 못했습니다.'
      throw error
    }
  }

  function roleForGroup(groupId: string) {
    return groups.value.find((group) => group.id === groupId)?.role
  }

  function canManageGroup(groupId: string) {
    const role = roleForGroup(groupId)
    return role === 'OWNER' || role === 'ADMIN'
  }

  function viewAllGroups() {
    allowNullSelection.value = true
    currentGroupId.value = null
  }

  function selectGroup(groupId: string) {
    const exists = groups.value.some((group) => group.id === groupId)
    if (!exists) return false
    allowNullSelection.value = false
    const changed = currentGroupId.value !== groupId
    currentGroupId.value = groupId
    if (changed) {
      fetchMembersOfGroup(groupId, { force: true })
    }
    return changed
  }

  async function fetchMembersOfGroup(groupId: string, options?: { force?: boolean }) {
    if (!groupId) return
    if (membersFetching.value.has(groupId) && !options?.force) return
    if (!options?.force && details.value[groupId]?.members) {
      return
    }
    setSetValue(membersFetching, groupId, true)
    setSetValue(memberErrors, groupId, false)
    try {
      const members = await groupApi.listMembers(groupId)
      applyMembers(groupId, members)
    } catch (error: any) {
      console.error('Failed to load members', error)
      setSetValue(memberErrors, groupId, true)
      errorMessage.value = error?.message ?? '멤버 목록을 불러오지 못했습니다.'
    } finally {
      setSetValue(membersFetching, groupId, false)
    }
  }

  async function addMemberToGroup(groupId: string, params: { email: string; role?: string }) {
    if (!groupId) return false
    setSetValue(memberOperations, groupId, true)
    errorMessage.value = null
    try {
      await groupApi.addMember(groupId, { email: params.email, role: params.role as any })
      await fetchMembersOfGroup(groupId, { force: true })
      return true
    } catch (error: any) {
      console.error('Failed to add member', error)
      errorMessage.value = error?.message ?? '멤버를 추가하지 못했습니다.'
      return false
    } finally {
      setSetValue(memberOperations, groupId, false)
    }
  }

  async function removeMember(groupId: string, membershipId: string) {
    setSetValue(memberOperations, groupId, true)
    try {
      await groupApi.removeMember(groupId, membershipId)
      await fetchMembersOfGroup(groupId, { force: true })
    } catch (error: any) {
      console.error('Failed to remove member', error)
      errorMessage.value = error?.message ?? '멤버를 삭제하지 못했습니다.'
      throw error
    } finally {
      setSetValue(memberOperations, groupId, false)
    }
  }

  const currentGroup = computed(() => {
    if (!currentGroupId.value) return null
    return groups.value.find((group) => group.id === currentGroupId.value) ?? null
  })

  function membersForGroup(groupId: string) {
    return details.value[groupId]?.members
  }

  function isFetchingMembers(groupId: string) {
    return membersFetching.value.has(groupId)
  }

  function hasMemberLoadError(groupId: string) {
    return memberErrors.value.has(groupId)
  }

  function isAddingMember(groupId: string) {
    return memberOperations.value.has(groupId)
  }

  function reconcileCurrentGroupSelection() {
    if (!groups.value.length) {
      currentGroupId.value = null
      return
    }
    if (
      currentGroupId.value &&
      groups.value.some((group) => group.id === currentGroupId.value)
    ) {
      return
    }
    if (allowNullSelection.value) {
      currentGroupId.value = null
      return
    }
    currentGroupId.value = groups.value[0]?.id ?? null
  }

  function storeDetail(detail: BackendGroupDetail) {
    const mappedGroup = mapSummary(detail)
    const mappedMembers = detail.members.map((member) => mapMember(detail.id, member))
    const record: GroupDetailRecord = {
      info: mappedGroup,
      invitationCode: detail.invitationCode,
      members: mappedMembers
    }
    details.value = {
      ...details.value,
      [mappedGroup.id]: record
    }
    updateGroupInList(mappedGroup)
    return record
  }

  function applyMembers(groupId: string, members: BackendGroupMember[]) {
    const mapped = members.map((member) => mapMember(groupId, member))
    const detail = details.value[groupId]
    if (detail) {
      details.value = {
        ...details.value,
        [groupId]: {
          ...detail,
          members: mapped
        }
      }
    } else {
      details.value = {
        ...details.value,
        [groupId]: {
          info: groups.value.find((group) => group.id === groupId) ?? mappedSummaryPlaceholder(groupId),
          invitationCode: '',
          members: mapped
        }
      }
    }
    const group = groups.value.find((item) => item.id === groupId)
    if (group) {
      group.memberCount = mapped.length
    }
  }

  function updateGroupInList(next: Group) {
    const exists = groups.value.some((group) => group.id === next.id)
    if (exists) {
      groups.value = groups.value.map((group) => (group.id === next.id ? next : group))
    } else {
      groups.value = [next, ...groups.value]
    }
  }

  return {
    groups,
    currentGroupId,
    currentGroup,
    isFetching,
    isCreating,
    errorMessage,
    init,
    fetchMyGroups,
    createGroup,
    updateGroup,
    joinGroupByCode,
    regenerateInvitation,
    selectGroup,
    viewAllGroups,
    addMemberToGroup,
    removeMember,
    fetchMembersOfGroup,
    membersForGroup,
    isFetchingMembers,
    hasMemberLoadError,
    isAddingMember,
    roleForGroup,
    canManageGroup
  }
})

function mapSummary(dto: BackendGroupSummary): Group {
  return {
    id: dto.id.toString(),
    name: dto.name,
    description: dto.description ?? null,
    status: dto.status,
    memberCount: dto.memberCount,
    role: dto.myRole,
    createdAt: dto.createdAt ? new Date(dto.createdAt) : null
  }
}

function mapMember(groupId: number | string, dto: BackendGroupMember): GroupMember {
  return {
    id: dto.membershipId.toString(),
    groupId: groupId.toString(),
    userId: dto.userId.toString(),
    email: dto.email,
    displayName: dto.displayName,
    role: dto.role,
    joinedAt: dto.joinedAt ? new Date(dto.joinedAt) : null
  }
}

function mappedSummaryPlaceholder(groupId: string): Group {
  return {
    id: groupId,
    name: '그룹',
    description: null,
    status: 'ACTIVE',
    memberCount: 0,
    role: 'MEMBER',
    createdAt: null
  }
}

function setSetValue(target: { value: Set<string> }, key: string, present: boolean) {
  const next = new Set(target.value)
  if (present) {
    next.add(key)
  } else {
    next.delete(key)
  }
  target.value = next
}
