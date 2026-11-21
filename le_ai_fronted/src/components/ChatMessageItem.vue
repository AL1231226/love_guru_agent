<script setup>
import { computed } from 'vue'
import { MODE_LOVE, MODE_META } from '../constants/modes'
import { renderMarkdown } from '../utils/markdown'
import ChatAvatar from './ChatAvatar.vue'

const props = defineProps({
  message: {
    type: Object,
    required: true,
  },
  mode: {
    type: String,
    required: true,
  },
})

const messageMode = computed(() => props.message.mode || props.mode)
const meta = computed(() => MODE_META[messageMode.value] || MODE_META[MODE_LOVE])
const isAssistant = computed(() => props.message.role === 'assistant')
const isThinking = computed(
  () => isAssistant.value && props.message.status === 'streaming' && !props.message.content,
)
const isStreaming = computed(() => props.message.status === 'streaming')
const rendered = computed(() =>
  isAssistant.value ? renderMarkdown(props.message.content) : '',
)
const author = computed(() => (isAssistant.value ? meta.value.assistantName : '你'))
const timeLabel = computed(() =>
  new Date(props.message.createdAt).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
  }),
)
</script>

<template>
  <article
    class="message"
    :class="[`message--${message.role}`, { 'is-error': message.status === 'error' }]"
    :data-mode="messageMode"
  >
    <ChatAvatar v-if="isAssistant" type="assistant" :mode="messageMode" />

    <div class="message__body">
      <div class="message__meta">
        <span class="message__author">{{ author }}</span>
        <span class="message__dot" aria-hidden="true">·</span>
        <time class="message__time">{{ timeLabel }}</time>
        <span v-if="message.status === 'stopped'" class="message__tag">已停止</span>
      </div>

      <div
        v-if="isThinking || message.content"
        class="message__bubble"
        :class="{ 'message__bubble--thinking': isThinking }"
      >
        <div v-if="isThinking" class="thinking" role="status" aria-label="AI 正在思考">
          <span></span><span></span><span></span>
        </div>

        <template v-else>
          <div
            v-if="isAssistant"
            class="markdown-body"
            :class="{ 'is-streaming': isStreaming }"
            v-html="rendered"
          ></div>
          <p v-else class="user-text">{{ message.content }}</p>
        </template>
      </div>

      <div v-if="message.status === 'error'" class="message__error" role="alert">
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.7" />
          <path
            d="M12 7.6v5.2"
            stroke="currentColor"
            stroke-width="1.9"
            stroke-linecap="round"
          />
          <circle cx="12" cy="16.3" r="1.05" fill="currentColor" />
        </svg>
        <div>
          <strong>回复生成失败</strong>
          <p>{{ message.error || '请确认后端服务已启动后重试。' }}</p>
        </div>
      </div>
    </div>

    <ChatAvatar v-if="!isAssistant" type="user" />
  </article>
</template>
