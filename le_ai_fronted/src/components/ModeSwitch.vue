<script setup>
import { MODE_AGENT, MODE_LOVE, MODE_META } from '../constants/modes'

defineProps({
  mode: {
    type: String,
    required: true,
  },
})

const emit = defineEmits(['change-mode'])
const options = [MODE_META[MODE_LOVE], MODE_META[MODE_AGENT]]

function select(nextMode) {
  emit('change-mode', nextMode)
}
</script>

<template>
  <div class="mode-switch" role="radiogroup" aria-label="对话模式">
    <button
      v-for="option in options"
      :key="option.id"
      class="mode-switch__option"
      :class="{ 'is-active': mode === option.id }"
      :data-mode="option.id"
      type="button"
      role="radio"
      :aria-checked="mode === option.id"
      :title="`${option.label}（切换后会开启新对话）`"
      @click="select(option.id)"
    >
      <svg v-if="option.id === MODE_LOVE" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path
          d="M4.5 5.7A2.7 2.7 0 0 1 7.2 3h9.6a2.7 2.7 0 0 1 2.7 2.7v6.9a2.7 2.7 0 0 1-2.7 2.7H9.9l-4 3.2a1.1 1.1 0 0 1-1.8-.9v-2.7a2.7 2.7 0 0 1-.4-1.4V5.7Z"
          fill="currentColor"
        />
      </svg>
      <svg v-else viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path
          d="M11 2.8 12.7 8l5.2 1.7-5.2 1.7L11 16.6l-1.7-5.2L4.1 9.7 9.3 8 11 2.8Z"
          fill="currentColor"
        />
        <path
          d="m18.4 13.6.9 2.7 2.7.9-2.7.9-.9 2.7-.9-2.7-2.7-.9 2.7-.9.9-2.7Z"
          fill="currentColor"
          opacity="0.78"
        />
      </svg>
      <span>{{ option.label }}</span>
    </button>
  </div>
</template>
