import { defineStore } from 'pinia'
import { ref, watch, type WatchStopHandle } from 'vue'
import {
  addDoc,
  collection,
  doc,
  onSnapshot,
  orderBy,
  query,
  serverTimestamp,
  setDoc
} from 'firebase/firestore'

import type { AppUser, ChatMessage, Friend, UserProfile } from '../types'
import { firestore } from '../firebase'
import type { useAuthStore } from './auth'
import * as userDirectory from '../services/userDirectory'

export const useSocialStore = defineStore('social', () => {
  const friends = ref<Friend[]>([])
  const directMessages = ref<Record<string, ChatMessage[]>>({})
  const groupMessages = ref<Record<string, ChatMessage[]>>({})
  const errorMessage = ref<string | null>(null)
  const initialized = ref(false)
  const currentUserId = ref<string | null>(null)

  let stopAuthWatch: WatchStopHandle | null = null
  let friendsUnsubscribe: (() => void) | null = null
  const directMessageUnsubs = new Map<string, () => void>()
  const groupMessageUnsubs = new Map<string, () => void>()

  function init(authStore: ReturnType<typeof useAuthStore>) {
    if (stopAuthWatch) return
    stopAuthWatch = watch(
      () => authStore.user?.uid ?? null,
      (uid) => {
        cleanupListeners()
        currentUserId.value = uid
        if (uid) {
          startFriendsListener(uid)
        } else {
          friends.value = []
          directMessages.value = {}
          groupMessages.value = {}
        }
      },
      { immediate: true }
    )
    initialized.value = true
  }

  function cleanupListeners() {
    if (friendsUnsubscribe) {
      friendsUnsubscribe()
      friendsUnsubscribe = null
    }
    directMessageUnsubs.forEach((unsub) => unsub())
    directMessageUnsubs.clear()
    groupMessageUnsubs.forEach((unsub) => unsub())
    groupMessageUnsubs.clear()
  }

  function startFriendsListener(uid: string) {
    const friendRef = collection(firestore, 'friendships', uid, 'items')
    friendsUnsubscribe = onSnapshot(friendRef, (snapshot) => {
      friends.value = snapshot.docs.map((docSnap) => {
        const data = docSnap.data()
        return {
          userId: docSnap.id,
          displayName: data.displayName ?? '친구',
          handle: data.handle ?? '',
          photoUrl: data.photoUrl ?? null
        }
      })
    })
  }

  async function addFriend(profile: UserProfile): Promise<Friend | null> {
    const uid = currentUserId.value
    if (!uid) {
      errorMessage.value = '로그인 후 이용해주세요.'
      return null
    }
    if (friends.value.some((friend) => friend.userId === profile.uid)) {
      errorMessage.value = '이미 추가된 친구입니다.'
      return null
    }
    const myProfile =
      userDirectory.profileForUid(uid) ?? (await userDirectory.fetchByUid(uid))
    if (!myProfile) {
      errorMessage.value = '내 프로필을 확인할 수 없습니다.'
      return null
    }

    await Promise.all([
      setDoc(doc(firestore, 'friendships', uid, 'items', profile.uid), {
        displayName: profile.displayName,
        handle: profile.handle,
        photoUrl: profile.photoUrl ?? null,
        userId: profile.uid,
        createdAt: serverTimestamp()
      }),
      setDoc(doc(firestore, 'friendships', profile.uid, 'items', uid), {
        displayName: myProfile.displayName,
        handle: myProfile.handle,
        photoUrl: myProfile.photoUrl ?? null,
        userId: uid,
        createdAt: serverTimestamp()
      })
    ])
    errorMessage.value = null
    ensureDirectMessages(profile.uid)
    return {
      userId: profile.uid,
      displayName: profile.displayName,
      handle: profile.handle,
      photoUrl: profile.photoUrl
    }
  }

  async function sendDirectMessage(params: {
    friendId: string
    sender: AppUser
    content: string
  }) {
    const trimmed = params.content.trim()
    if (!trimmed) return
    const uid = currentUserId.value
    if (!uid) {
      errorMessage.value = '로그인 후 이용해주세요.'
      return
    }
    const conversationId = getConversationId(uid, params.friendId)
    await addDoc(collection(firestore, 'directMessages', conversationId, 'messages'), {
      senderId: params.sender.uid,
      senderName: params.sender.displayName ?? '나',
      content: trimmed,
      sentAt: serverTimestamp(),
      channelType: 'direct',
      channelId: conversationId
    })
    ensureDirectMessages(params.friendId)
  }

  async function sendGroupMessage(params: {
    groupId: string
    groupName: string
    sender: AppUser
    content: string
  }) {
    const trimmed = params.content.trim()
    if (!trimmed) return
    await addDoc(collection(firestore, 'groupMessages', params.groupId, 'messages'), {
      senderId: params.sender.uid,
      senderName: params.sender.displayName ?? '나',
      content: trimmed,
      sentAt: serverTimestamp(),
      channelType: 'group',
      channelId: params.groupId
    })
    ensureGroupMessages(params.groupId)
  }

  function ensureDirectMessages(friendId: string) {
    const uid = currentUserId.value
    if (!uid || directMessageUnsubs.has(friendId)) return
    const conversationId = getConversationId(uid, friendId)
    const q = query(
      collection(firestore, 'directMessages', conversationId, 'messages'),
      orderBy('sentAt', 'asc')
    )
    const unsubscribe = onSnapshot(q, (snapshot) => {
      directMessages.value = {
        ...directMessages.value,
        [friendId]: snapshot.docs.map((docSnap) => mapMessage(docSnap.id, docSnap.data()))
      }
    })
    directMessageUnsubs.set(friendId, unsubscribe)
  }

  function ensureGroupMessages(groupId: string) {
    if (groupMessageUnsubs.has(groupId)) return
    const q = query(
      collection(firestore, 'groupMessages', groupId, 'messages'),
      orderBy('sentAt', 'asc')
    )
    const unsubscribe = onSnapshot(q, (snapshot) => {
      groupMessages.value = {
        ...groupMessages.value,
        [groupId]: snapshot.docs.map((docSnap) => mapMessage(docSnap.id, docSnap.data()))
      }
    })
    groupMessageUnsubs.set(groupId, unsubscribe)
  }

  function messagesForFriend(friendId: string): ChatMessage[] {
    return directMessages.value[friendId] ?? []
  }

  function groupMessagesFor(groupId: string): ChatMessage[] {
    return groupMessages.value[groupId] ?? []
  }

  function mapMessage(id: string, data: any): ChatMessage {
    return {
      id,
      senderId: data.senderId ?? 'unknown',
      senderName: data.senderName ?? 'unknown',
      content: data.content ?? '',
      sentAt: data.sentAt?.toDate?.() ?? new Date(),
      channelType: data.channelType ?? 'direct',
      channelId: data.channelId ?? ''
    }
  }

  function getConversationId(uid: string, friendId: string) {
    return [uid, friendId].sort().join('__')
  }

  return {
    friends,
    errorMessage,
    init,
    addFriend,
    sendDirectMessage,
    sendGroupMessage,
    ensureDirectMessages,
    ensureGroupMessages,
    messagesForFriend,
    groupMessagesFor
  }
})
