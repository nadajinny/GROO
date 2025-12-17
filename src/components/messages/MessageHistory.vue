<template>
  <div ref="historyContainer" class="history">
    <div v-if="!messages.length" class="empty-state">
      아직 메시지가 없습니다. 첫 메시지를 보내보세요!
    </div>
    <div v-else class="history-list">
      <div
        v-for="message in messages"
        :key="message.id"
        :class="['message', { mine: message.senderId === currentUserId }]"
      >
        <div class="message__sender">
          {{ message.senderId === currentUserId ? '나' : message.senderName }}
        </div>
        <div class="message__content">{{ message.content }}</div>
        <div class="message__time">{{ formatTime(message.sentAt) }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import type { ChatMessage } from '../../types'

const props = defineProps<{
  messages: ChatMessage[]
  currentUserId: string | null
}>()

const historyContainer = ref<HTMLDivElement | null>(null)

watch(
  () => props.messages,
  () => scrollToBottom(),
  { immediate: true, deep: true }
)

function scrollToBottom() {
  nextTick(() => {
    const el = historyContainer.value
    if (el) {
      el.scrollTop = el.scrollHeight
    }
  })
}

function formatTime(date: Date) {
  const pad = (value: number) => `${value}`.padStart(2, '0')
  return `${pad(date.getHours())}:${pad(date.getMinutes())}`
}
</script>

<style scoped>
.history {
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius);
  padding: 12px;
  min-height: 220px;
  max-height: 320px;
  overflow-y: auto;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message {
  align-self: flex-start;
  max-width: 80%;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 14px;
  padding: 10px 14px;
}

.message.mine {
  align-self: flex-end;
  background: rgba(255, 255, 255, 0.12);
}

.message__sender {
  font-size: 13px;
  color: var(--app-text-muted);
}

.message__content {
  margin: 6px 0;
}

.message__time {
  font-size: 12px;
  color: var(--app-text-muted);
  text-align: right;
}
</style>
