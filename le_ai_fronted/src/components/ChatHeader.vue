<script setup>
import { computed, onBeforeUnmount, ref } from 'vue'
import { MODE_LOVE, MODE_META } from '../constants/modes'
import { copyText } from '../utils/clipboard'

const props = defineProps({
  mode: {
    type: String,
    required: true,
  },
  chatId: {
    type: String,
    required: true,
  },
  streaming: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['new-chat'])

const meta = computed(() => MODE_META[props.mode] || MODE_META[MODE_LOVE])
const shortChatId = computed(() => {
  const id = props.chatId || ''
  return id.length > 12 ? `${id.slice(0, 8)}…` : id
})

const copied = ref(false)
let copyTimer = null

async function handleCopy() {
  const ok = await copyText(props.chatId)
  if (!ok) return

  copied.value = true
  clearTimeout(copyTimer)
  copyTimer = setTimeout(() => {
    copied.value = false
  }, 1600)
}

onBeforeUnmount(() => clearTimeout(copyTimer))
</script>

<template>
  <header class="chat-header">
    <div class="brand">
      <div class="brand__mark" :class="`brand__mark--${mode}`" aria-hidden="true">
        <svg v-if="mode === MODE_LOVE" viewBox="0 0 24 24" fill="none">
          <path
            d="M12 20.3S4.7 15.9 2.5 12.2C.9 9.1 2.6 5.3 6.2 5.3c2 0 3.4 1.1 4.2 2.3.8-1.2 2.2-2.3 4.2-2.3 3.6 0 5.3 3.8 3.7 6.9-2.2 3.7-9.5 8.1-9.5 8.1Z"
            fill="currentColor"
          />
        </svg>
        <svg v-else viewBox="0 0 24 24" fill="none">
          <path
            d="m12 2.8 2 5.7 5.7 2-5.7 2-2 5.7-2-5.7-5.7-2 5.7-2 2-5.7Z"
            fill="currentColor"
          />
          <path
            d="m19 14.2.9 2.8 2.8.9-2.8.9-.9 2.8-.9-2.8-2.8-.9 2.8-.9.9-2.8Z"
            fill="currentColor"
            opacity="0.75"
          />
        </svg>
      </div>

      <div class="brand__text">
        <h1 class="brand__title">
          恋爱大师
          <span class="brand__badge" :class="`brand__badge--${mode}`">AI</span>
        </h1>
        <p class="brand__tagline">{{ meta.tagline }}</p>
      </div>
    </div>

    <div class="header-actions">
      <div
        class="session-chip"
        :class="{ 'is-streaming': streaming }"
        :title="`会话 ID：${chatId}`"
      >
        <span class="session-chip__dot" aria-hidden="true"></span>
        <span class="session-chip__mode">{{ meta.shortLabel }}</span>
        <code class="session-chip__id">{{ shortChatId }}</code>
        <button
          class="session-chip__copy"
          type="button"
          :aria-label="copied ? '会话 ID 已复制' : '复制会话 ID'"
          :title="copied ? '已复制' : '复制完整会话 ID'"
          @click="handleCopy"
        >
          <svg v-if="!copied" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <rect
              x="9"
              y="9"
              width="11"
              height="11"
              rx="2.4"
              stroke="currentColor"
              stroke-width="1.6"
            />
            <path
              d="M15 9V6.4A2.4 2.4 0 0 0 12.6 4H6.4A2.4 2.4 0 0 0 4 6.4v6.2A2.4 2.4 0 0 0 6.4 15H9"
              stroke="currentColor"
              stroke-width="1.6"
              stroke-linecap="round"
            />
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path
              d="m5 12.5 4.2 4.2L19 7"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </button>
      </div>

      <button
        class="new-chat-button"
        type="button"
        aria-label="新建对话"
        title="新建对话"
        @click="$emit('new-chat')"
      >
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path
            d="M12 5v14M5 12h14"
            stroke="currentColor"
            stroke-width="1.9"
            stroke-linecap="round"
          />
        </svg>
        <span>新对话</span>
      </button>
    </div>
  </header>
</template>
