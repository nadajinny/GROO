<template>
  <Teleport to="body">
    <Transition name="dialog-fade">
      <div
        v-if="modelValue"
        class="app-dialog__backdrop"
        role="presentation"
        @click="handleBackdrop"
      >
        <div
          class="app-dialog"
          :style="{ maxWidth: width }"
          role="dialog"
          aria-modal="true"
          @click.stop
        >
          <header v-if="showHeader" class="app-dialog__header">
            <slot name="header">
              <h3 v-if="title">{{ title }}</h3>
            </slot>
            <button type="button" class="app-dialog__close" @click="close" aria-label="닫기">
              ✕
            </button>
          </header>
          <div class="app-dialog__body">
            <slot />
          </div>
          <footer v-if="$slots.footer" class="app-dialog__footer">
            <slot name="footer" />
          </footer>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, useSlots } from 'vue'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    title?: string
    width?: string
    closeOnBackdrop?: boolean
  }>(),
  {
    width: '420px',
    closeOnBackdrop: true
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const slots = useSlots()
const showHeader = computed(() => !!props.title || !!slots.header)

function close() {
  emit('update:modelValue', false)
}

function handleBackdrop() {
  if (!props.closeOnBackdrop) return
  close()
}
</script>

<style scoped>
.app-dialog__backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.65);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 24px;
}

.app-dialog {
  width: 100%;
  background: var(--app-surface, #111827);
  border-radius: 20px;
  border: 1px solid var(--app-border);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.45);
  padding: 24px;
}

.app-dialog__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.app-dialog__close {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 1px solid var(--app-border);
  background: transparent;
  color: inherit;
  cursor: pointer;
}

.app-dialog__body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.app-dialog__footer {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: opacity 0.2s ease;
}

.dialog-fade-enter-from,
.dialog-fade-leave-to {
  opacity: 0;
}
</style>
