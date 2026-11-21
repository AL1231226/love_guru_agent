<script setup>
import { computed } from 'vue'
import { MODE_LOVE } from '../constants/modes'

const props = defineProps({
  type: {
    type: String,
    default: 'assistant',
    validator: (value) => ['assistant', 'user'].includes(value),
  },
  mode: {
    type: String,
    default: MODE_LOVE,
  },
  size: {
    type: Number,
    default: 38,
  },
})

const label = computed(() => {
  if (props.type === 'user') return '你'
  return props.mode === MODE_LOVE ? '恋爱大师' : 'LeManus'
})
</script>

<template>
  <span
    class="avatar"
    :class="[`avatar--${type}`, type === 'assistant' ? `avatar--${mode}` : '']"
    :style="{ '--avatar-size': `${size}px` }"
    role="img"
    :aria-label="label"
  >
    <svg v-if="type === 'user'" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="12" cy="8.1" r="3.7" fill="currentColor" />
      <path
        d="M4.7 20.2c.55-3.8 3.5-6.4 7.3-6.4s6.75 2.6 7.3 6.4"
        fill="currentColor"
      />
    </svg>

    <svg
      v-else-if="mode === MODE_LOVE"
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden="true"
    >
      <path
        d="M12 20.3S4.7 15.9 2.5 12.2C.9 9.1 2.6 5.3 6.2 5.3c2 0 3.4 1.1 4.2 2.3.8-1.2 2.2-2.3 4.2-2.3 3.6 0 5.3 3.8 3.7 6.9-2.2 3.7-9.5 8.1-9.5 8.1Z"
        fill="currentColor"
      />
    </svg>

    <svg v-else viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M12 3.1v1.7"
        stroke="currentColor"
        stroke-width="1.8"
        stroke-linecap="round"
      />
      <circle cx="12" cy="2.3" r="1.15" fill="currentColor" />
      <rect x="4.2" y="5.1" width="15.6" height="12.5" rx="4.1" fill="currentColor" />
      <circle cx="9" cy="11" r="1.35" fill="#fff" />
      <circle cx="15" cy="11" r="1.35" fill="#fff" />
      <rect x="8" y="14.2" width="8" height="1.55" rx="0.775" fill="#fff" opacity="0.88" />
    </svg>
  </span>
</template>
