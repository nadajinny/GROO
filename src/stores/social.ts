import { defineStore } from 'pinia'
import { ref } from 'vue'

import type { AppUser, ChatMessage, Friend, UserProfile } from '../types'

let messageCounter = 0

export const useSocialStore = defineStore('social', () => {
  const friends = ref<Friend[]>([])
  const directMessages = ref<Record<string, ChatMessage[]>>({})
  const groupMessages = ref<Record<string, ChatMessage[]>>({})
  const errorMessage = ref<string | null>(null)
  const initialized = ref(false)

  function init() {
    if (initialized.value) return
    seedMockData()
    initialized.value = true
  }

  function seedMockData() {
    const seeds: Friend[] = [
      {
        userId: 'alex.dev.seed',
        displayName: 'Alex Dev',
        handle: 'alex.dev#10001'
      },
      {
        userId: 'minji.pm.seed',
        displayName: 'Minji PM',
        handle: 'minji.pm#20001'
      }
    ]
    friends.value = seeds
    const [alex, minji] = seeds
    const map: Record<string, ChatMessage[]> = {}
    if (alex) {
      map[alex.userId] = [
        {
          id: nextMessageId(),
          senderId: alex.userId,
          senderName: alex.displayName,
          content: '이번 주 스프린트 보드 확인했어?',
          sentAt: new Date(Date.now() - 42 * 60 * 1000),
          channelType: 'direct',
          channelId: alex.userId
        },
        {
          id: nextMessageId(),
          senderId: alex.userId,
          senderName: alex.displayName,
          content: '데이터 파이프라인 태스크 도움이 필요하면 알려줘!',
          sentAt: new Date(Date.now() - 38 * 60 * 1000),
          channelType: 'direct',
          channelId: alex.userId
        }
      ]
    }
    if (minji) {
      map[minji.userId] = [
        {
          id: nextMessageId(),
          senderId: minji.userId,
          senderName: minji.displayName,
          content: '회의 초대 보냈어. 시간 괜찮니?',
          sentAt: new Date(Date.now() - 2 * 60 * 60 * 1000),
          channelType: 'direct',
          channelId: minji.userId
        }
      ]
    }
    directMessages.value = map
  }

  async function addFriend(profile: UserProfile): Promise<Friend | null> {
    if (friends.value.some((friend) => friend.userId === profile.uid)) {
      errorMessage.value = '이미 추가된 친구입니다.'
      return null
    }
    const friend: Friend = {
      userId: profile.uid,
      displayName: profile.displayName,
      handle: profile.handle,
      photoUrl: profile.photoUrl
    }
    friends.value = [...friends.value, friend]
    directMessages.value = { ...directMessages.value, [friend.userId]: [] }
    errorMessage.value = null
    return friend
  }

  async function sendDirectMessage(params: {
    friendId: string
    sender: AppUser
    content: string
  }) {
    const trimmed = params.content.trim()
    if (!trimmed) return
    const existing = directMessages.value[params.friendId] ?? []
    const message: ChatMessage = {
      id: nextMessageId(),
      senderId: params.sender.uid,
      senderName: params.sender.displayName ?? '나',
      content: trimmed,
      sentAt: new Date(),
      channelType: 'direct',
      channelId: params.friendId
    }
    directMessages.value = {
      ...directMessages.value,
      [params.friendId]: [...existing, message]
    }
  }

  async function sendGroupMessage(params: {
    groupId: string
    groupName: string
    sender: AppUser
    content: string
  }) {
    const trimmed = params.content.trim()
    if (!trimmed) return
    const existing = groupMessages.value[params.groupId] ?? []
    const message: ChatMessage = {
      id: nextMessageId(),
      senderId: params.sender.uid,
      senderName: params.sender.displayName ?? '나',
      content: trimmed,
      sentAt: new Date(),
      channelType: 'group',
      channelId: params.groupId
    }
    groupMessages.value = {
      ...groupMessages.value,
      [params.groupId]: [...existing, message]
    }
  }

  function messagesForFriend(friendId: string): ChatMessage[] {
    return directMessages.value[friendId] ?? []
  }

  function groupMessagesFor(groupId: string): ChatMessage[] {
    return groupMessages.value[groupId] ?? []
  }

  function nextMessageId() {
    return `msg_${messageCounter++}`
  }

  return {
    friends,
    errorMessage,
    init,
    addFriend,
    sendDirectMessage,
    sendGroupMessage,
    messagesForFriend,
    groupMessagesFor
  }
})
