<template>
  <div class="composer">
    <input
      v-model="localValue"
      class="input"
      type="text"
      :placeholder="placeholder"
      :disabled="disabled"
      @keyup.enter="send"
    />
    <button class="btn btn-primary" :disabled="disabled || !localValue.trim()" @click="send">
      보내기
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  modelValue: string
  placeholder?: string
  disabled?: boolean
}>()
const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'send'): void
}>()

const localValue = computed({
  get: () => props.modelValue,
  set: (value: string) => emit('update:modelValue', value)
})

function send() {
  if (!localValue.value.trim() || props.disabled) return
  emit('send')
}
</script>

<style scoped>
.composer {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}
</style>
