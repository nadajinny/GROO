<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-header__title">메시지</h2>
      <p class="page-header__subtitle">
        좌측에서 채널을 선택해 친구 혹은 그룹 전체와 대화를 나눠보세요.
      </p>
    </div>

    <div class="messages-layout">
      <aside class="messages-sidebar app-card">
        <div
          class="group-card"
          :class="{ active: isGroupSelected }"
          @click="selectGroupChannel"
        >
          <div>
            <strong>그룹 전체 메시지</strong>
            <p class="text-muted">
              {{
                groupStore.currentGroup
                  ? groupStore.currentGroup.name
                  : '현재 작업 그룹을 선택하세요.'
              }}
            </p>
          </div>
          <span class="pill">{{ isGroupSelected ? '선택됨' : '선택' }}</span>
        </div>

        <button class="btn btn-primary" @click="openAddFriendDialog">친구 추가</button>
        <div v-if="socialStore.errorMessage" class="error-banner">
          {{ socialStore.errorMessage }}
        </div>

        <h4>친구 목록</h4>
        <ul class="list-reset friend-list">
          <li
            v-for="friend in socialStore.friends"
            :key="friend.userId"
            :class="[
              'friend-item',
              { active: selectedFriendId === friend.userId && !isGroupSelected }
            ]"
            @click="selectFriend(friend.userId)"
          >
            <div>
              <div class="friend-name">{{ friend.displayName }}</div>
              <div class="friend-handle">{{ friend.handle }}</div>
            </div>
            <span class="pill">채팅</span>
          </li>
        </ul>

        <div v-if="!socialStore.friends.length" class="empty-state">
          아직 추가된 친구가 없습니다.
        </div>
      </aside>

      <section class="messages-panel app-card">
        <Transition name="panel-fade" mode="out-in">
        <div v-if="isGroupSelected" key="group">
          <h3>그룹 메시지</h3>
          <p class="text-muted">
            {{
              groupStore.currentGroup
                ? `${groupStore.currentGroup.name} 그룹 멤버 전체에게 전달됩니다.`
                : '좌측에서 현재 작업할 그룹을 선택하세요.'
            }}
          </p>
          <MessageHistory
            :messages="groupStore.currentGroup ? socialStore.groupMessagesFor(groupStore.currentGroup.id) : []"
            :current-user-id="currentUserId"
          />
          <MessageComposer
            placeholder="전체에게 보내기"
            :disabled="!groupStore.currentGroup || !currentUserId"
            v-model="groupMessage"
            @send="sendGroupMessage"
          />
        </div>
        <div v-else key="direct">
          <h3>개인 메시지</h3>
          <p class="text-muted">
            {{
              selectedFriend
                ? `${selectedFriend.displayName}님과의 대화`
                : '친구를 선택하면 대화 내용이 표시됩니다.'
            }}
          </p>
          <MessageHistory
            :messages="selectedFriendId ? socialStore.messagesForFriend(selectedFriendId) : []"
            :current-user-id="currentUserId"
          />
          <MessageComposer
            placeholder="메시지를 입력하세요"
            :disabled="!selectedFriendId || !currentUserId"
            v-model="directMessage"
            @send="sendDirectMessage"
          />
        </div>
        </Transition>
      </section>
    </div>

    <AppDialog v-model="addFriendDialog" title="새 친구 추가">
      <input v-model="friendHandle" class="input" type="text" placeholder="예: teammate#12345" />
      <p class="text-muted">핸들 형식은 이름#숫자 입니다.</p>
      <p v-if="friendError" class="text-muted">{{ friendError }}</p>
      <template #footer>
        <button class="btn btn-outline" @click="closeAddFriendDialog">취소</button>
        <button class="btn btn-primary" @click="submitAddFriend">추가</button>
      </template>
    </AppDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import { useAuthStore } from '../stores/auth'
import { useGroupStore } from '../stores/group'
import { useSocialStore } from '../stores/social'
import * as userDirectory from '../services/userDirectory'

import MessageComposer from '../components/messages/MessageComposer.vue'
import MessageHistory from '../components/messages/MessageHistory.vue'
import AppDialog from '../components/common/AppDialog.vue'

const authStore = useAuthStore()
const groupStore = useGroupStore()
const socialStore = useSocialStore()

const selectedFriendId = ref<string | null>(null)
const isGroupSelected = ref(true)
const directMessage = ref('')
const groupMessage = ref('')
const addFriendDialog = ref(false)
const friendHandle = ref('')
const friendError = ref<string | null>(null)
const sendingDirect = ref(false)
const sendingGroup = ref(false)

const currentUserId = computed(() => authStore.user?.uid ?? null)
const selectedFriend = computed(() =>
  socialStore.friends.find((friend) => friend.userId === selectedFriendId.value)
)

watch(
  () => [groupStore.currentGroupId, isGroupSelected.value] as const,
  ([groupId, isGroup]) => {
    if (isGroup && groupId) {
      socialStore.ensureGroupMessages(groupId)
    }
  },
  { immediate: true }
)

function selectFriend(id: string) {
  selectedFriendId.value = id
  isGroupSelected.value = false
  socialStore.ensureDirectMessages(id)
}

function selectGroupChannel() {
  isGroupSelected.value = true
  if (groupStore.currentGroup) {
    socialStore.ensureGroupMessages(groupStore.currentGroup.id)
  }
}

async function sendDirectMessage() {
  if (!selectedFriendId.value || !authStore.user || sendingDirect.value) return
  sendingDirect.value = true
  try {
    await socialStore.sendDirectMessage({
      friendId: selectedFriendId.value,
      sender: authStore.user,
      content: directMessage.value
    })
    directMessage.value = ''
  } finally {
    sendingDirect.value = false
  }
}

async function sendGroupMessage() {
  if (!groupStore.currentGroup || !authStore.user || sendingGroup.value) return
  sendingGroup.value = true
  try {
    await socialStore.sendGroupMessage({
      groupId: groupStore.currentGroup.id,
      groupName: groupStore.currentGroup.name,
      sender: authStore.user,
      content: groupMessage.value
    })
    groupMessage.value = ''
  } finally {
    sendingGroup.value = false
  }
}

function openAddFriendDialog() {
  addFriendDialog.value = true
  friendHandle.value = ''
  friendError.value = null
}

function closeAddFriendDialog() {
  addFriendDialog.value = false
}

async function submitAddFriend() {
  const profile = await userDirectory.fetchByHandle(friendHandle.value)
  if (!profile) {
    friendError.value = '해당 핸들을 가진 사용자를 찾을 수 없습니다.'
    return
  }
  const friend = await socialStore.addFriend(profile)
  if (friend) {
    selectedFriendId.value = friend.userId
    isGroupSelected.value = false
    closeAddFriendDialog()
  } else if (socialStore.errorMessage) {
    friendError.value = socialStore.errorMessage
  }
}
</script>

<style scoped>
.messages-layout {
  display: grid;
  grid-template-columns: 340px 1fr;
  gap: 24px;
}

@media (max-width: 1024px) {
  .messages-layout {
    grid-template-columns: 1fr;
  }
}

.messages-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.group-card {
  border: 1px solid var(--app-border);
  border-radius: 16px;
  padding: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.group-card.active {
  border-color: rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.04);
}

.friend-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.friend-item {
  border: 1px solid var(--app-border);
  border-radius: 14px;
  padding: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.friend-item.active {
  border-color: rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.04);
}

.friend-handle {
  color: var(--app-text-muted);
  font-size: 13px;
}

.messages-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.error-banner {
  border-radius: 12px;
  padding: 12px;
  background: rgba(255, 107, 107, 0.15);
  border: 1px solid rgba(255, 107, 107, 0.4);
}

.panel-fade-enter-active,
.panel-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.panel-fade-enter-from,
.panel-fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
