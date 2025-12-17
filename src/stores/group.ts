import { defineStore } from 'pinia'
import { ref, computed, watch, type WatchStopHandle } from 'vue'
import {
  addDoc,
  collection,
  doc,
  getDoc,
  getDocs,
  orderBy,
  query,
  serverTimestamp,
  setDoc,
  where
} from 'firebase/firestore'

import { firestore } from '../firebase'
import type { Group, GroupMember, Role } from '../types'
import { roleFromString, roleToString } from '../types'
import type { useAuthStore } from './auth'
import * as userDirectory from '../services/userDirectory'

const groupsCollection = collection(firestore, 'groups')
const groupMembersCollection = collection(firestore, 'groupMembers')

export const useGroupStore = defineStore('groups', () => {
  const groups = ref<Group[]>([])
  const currentGroupId = ref<string | null>(null)
  const allowNullSelection = ref(false)
  const isFetching = ref(false)
  const isCreating = ref(false)
  const errorMessage = ref<string | null>(null)
  const membershipRoles = ref<Record<string, Role>>({})
  const memberOperations = ref<Set<string>>(new Set())
  const groupMembers = ref<Record<string, GroupMember[]>>({})
  const membersFetching = ref<Set<string>>(new Set())
  const memberErrors = ref<Set<string>>(new Set())
  const authStoreRef = ref<ReturnType<typeof useAuthStore> | null>(null)
  let stopAuthWatch: WatchStopHandle | null = null

  function init(authStore: ReturnType<typeof useAuthStore>) {
    if (stopAuthWatch) return
    authStoreRef.value = authStore
    stopAuthWatch = watch(
      () => authStore.user?.uid ?? null,
      (uid) => {
        if (!uid) {
          groups.value = []
          membershipRoles.value = {}
          currentGroupId.value = null
          allowNullSelection.value = false
          groupMembers.value = {}
          memberErrors.value = new Set()
          membersFetching.value = new Set()
          errorMessage.value = null
        } else {
          fetchMyGroups(uid)
        }
      },
      { immediate: true }
    )
  }

  function roleForGroup(id: string): Role | undefined {
    return membershipRoles.value[id]
  }

  const currentGroup = computed(() => {
    if (!currentGroupId.value) return null
    return groups.value.find((group) => group.id === currentGroupId.value) ?? null
  })

  function canManageGroup(groupId: string): boolean {
    const role = membershipRoles.value[groupId]
    if (role === 'owner') return true
    const currentUserId = authStoreRef.value?.user?.uid
    if (!currentUserId) return false
    const group = groups.value.find((item) => item.id === groupId)
    return group?.createdBy === currentUserId
  }

  async function fetchMyGroups(userId?: string) {
    const uid = userId ?? authStoreRef.value?.user?.uid
    if (!uid) {
      errorMessage.value = '로그인이 필요합니다.'
      return
    }
    isFetching.value = true
    errorMessage.value = null
    try {
      const membershipsSnap = await getDocs(
        query(groupMembersCollection, where('userId', '==', uid))
      )
      const roles: Record<string, Role> = {}
      const groupIds: string[] = []
      membershipsSnap.forEach((doc) => {
        const data = doc.data()
        const groupId = data.groupId as string
        if (groupId) {
          groupIds.push(groupId)
          roles[groupId] = roleFromString(data.role)
        }
      })
      membershipRoles.value = roles

      if (!groupIds.length) {
        groups.value = []
        currentGroupId.value = null
        allowNullSelection.value = false
        return
      }

      const groupDocs = await Promise.all(
        groupIds.map((id) => getDoc(doc(groupsCollection, id)))
      )

      const mapped = groupDocs
        .filter((snapshot) => snapshot.exists())
        .map((snapshot) => {
          const data = snapshot.data()
          return {
            id: snapshot.id,
            name: data?.name ?? '이름 없음',
            createdBy: data?.createdBy ?? '',
            createdAt: data?.createdAt ? data.createdAt.toDate() : null
          } as Group
        })
        .sort((a, b) => {
          const aTime = a.createdAt?.getTime() ?? 0
          const bTime = b.createdAt?.getTime() ?? 0
          return bTime - aTime
        })
      groups.value = mapped
      reconcileCurrentGroupSelection()
    } catch (error) {
      console.error('Failed to fetch groups', error)
      errorMessage.value = '그룹을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.'
    } finally {
      isFetching.value = false
    }
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
    const firstGroup = groups.value[0]
    if (firstGroup) {
      currentGroupId.value = firstGroup.id
    }
  }

  function selectGroup(groupId: string): boolean {
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

  function viewAllGroups() {
    allowNullSelection.value = true
    currentGroupId.value = null
  }

  async function createGroup(name: string): Promise<string | null> {
    const uid = authStoreRef.value?.user?.uid
    if (!uid) {
      errorMessage.value = '로그인 후 그룹을 만들 수 있습니다.'
      return null
    }
    const trimmed = name.trim()
    if (!trimmed) {
      errorMessage.value = '그룹 이름을 입력해주세요.'
      return null
    }
    isCreating.value = true
    errorMessage.value = null
    try {
      const docRef = await addDoc(groupsCollection, {
        name: trimmed,
        createdBy: uid,
        createdAt: serverTimestamp()
      })
      await setDoc(doc(groupMembersCollection, `${docRef.id}_${uid}`), {
        groupId: docRef.id,
        userId: uid,
        role: roleToString('owner')
      })
      await fetchMyGroups(uid)
      selectGroup(docRef.id)
      return docRef.id
    } catch (error) {
      console.error('Failed to create group', error)
      errorMessage.value = '그룹 생성에 실패했습니다. 잠시 후 다시 시도해주세요.'
      return null
    } finally {
      isCreating.value = false
    }
  }

  async function addMemberToGroup(params: {
    group: Group
    memberUserId: string
    role?: Role
  }): Promise<boolean> {
    const currentUserId = authStoreRef.value?.user?.uid
    if (!currentUserId) {
      errorMessage.value = '로그인 후에만 멤버를 추가할 수 있습니다.'
      return false
    }
    if (!canManageGroup(params.group.id)) {
      errorMessage.value = '그룹 owner만 멤버를 추가할 수 있습니다.'
      return false
    }
    const trimmed = params.memberUserId.trim()
    if (!trimmed) {
      errorMessage.value = '추가할 사용자를 찾을 수 없습니다.'
      return false
    }
    if (trimmed === currentUserId) {
      errorMessage.value = '본인은 이미 그룹 멤버입니다.'
      return false
    }
    const memberDocId = `${params.group.id}_${trimmed}`
    memberOperations.value.add(params.group.id)
    errorMessage.value = null
    try {
      const existing = await getDoc(doc(groupMembersCollection, memberDocId))
      if (existing.exists()) {
        errorMessage.value = '이미 해당 그룹에 속한 사용자입니다.'
        return false
      }
      await setDoc(doc(groupMembersCollection, memberDocId), {
        groupId: params.group.id,
        userId: trimmed,
        role: roleToString(params.role ?? 'member')
      })
      fetchMembersOfGroup(params.group.id, { force: true })
      return true
    } catch (error) {
      console.error('Failed to add member', error)
      errorMessage.value = '멤버 추가에 실패했습니다. 잠시 후 다시 시도해주세요.'
      return false
    } finally {
      memberOperations.value.delete(params.group.id)
    }
  }

  async function fetchMembersOfGroup(groupId: string, options?: { force?: boolean }) {
    if (!groupId) return
    const cache = groupMembers.value[groupId]
    if (cache && !options?.force) return
    if (membersFetching.value.has(groupId)) return

    membersFetching.value.add(groupId)
    memberErrors.value.delete(groupId)
    try {
      const membersSnap = await getDocs(
        query(groupMembersCollection, where('groupId', '==', groupId), orderBy('role'))
      )
      const members: GroupMember[] = []
      for (const docSnap of membersSnap.docs) {
        const data = docSnap.data()
        const member: GroupMember = {
          groupId,
          userId: data.userId ?? '',
          role: roleFromString(data.role),
          handle: data.handle ?? null
        }
        if (!member.handle && member.userId) {
          const profile = await userDirectory.fetchByUid(member.userId)
          member.handle = profile?.handle ?? null
        }
        members.push(member)
      }
      groupMembers.value = { ...groupMembers.value, [groupId]: members }
    } catch (error) {
      console.error('Failed to fetch members', error)
      memberErrors.value.add(groupId)
    } finally {
      membersFetching.value.delete(groupId)
    }
  }

  function membersForGroup(groupId: string): GroupMember[] | undefined {
    return groupMembers.value[groupId]
  }

  function isFetchingMembers(groupId: string): boolean {
    return membersFetching.value.has(groupId)
  }

  function hasMemberLoadError(groupId: string): boolean {
    return memberErrors.value.has(groupId)
  }

  function isAddingMember(groupId: string): boolean {
    return memberOperations.value.has(groupId)
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
    selectGroup,
    viewAllGroups,
    createGroup,
    addMemberToGroup,
    membersForGroup,
    isFetchingMembers,
    hasMemberLoadError,
    isAddingMember,
    roleForGroup,
    canManageGroup,
    fetchMembersOfGroup
  }
})
