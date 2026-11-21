<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { MODE_LOVE, MODE_META } from '../constants/modes'
import ModeSwitch from './ModeSwitch.vue'

const props = defineProps({
  mode: {
    type: String,
    required: true,
  },
  streaming: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['send', 'stop', 'change-mode'])

const draft = ref('')
const textareaRef = ref(null)
const meta = computed(() => MODE_META[props.mode] || MODE_META[MODE_LOVE])
const canSend = computed(() => draft.value.trim().length > 0 && !props.streaming)

function resize() {
  const el = textareaRef.value
  if (!el) return

  el.style.height = 'auto'
  el.style.height = `${Math.min(el.scrollHeight, 168)}px`
}

function submit() {
  const text = draft.value.trim()
  if (!text || props.streaming) return

  emit('send', text)
  draft.value = ''
  nextTick(() => {
    resize()
    textareaRef.value?.focus()
  })
}

function handleKeydown(event) {
  if (event.key !== 'Enter' || event.shiftKey) return
  if (event.isComposing || event.keyCode === 229) return

  event.preventDefault()
  submit()
}

function handleAction() {
  if (props.streaming) {
    emit('stop')
  } else {
    submit()
  }
}

function chooseMode(nextMode) {
  if (nextMode === props.mode) return
  emit('change-mode', nextMode)
}

onMounted(() => {
  resize()
  if (window.matchMedia?.('(pointer: fine)').matches) {
    textareaRef.value?.focus()
  }
})
</script>

<template>
  <div class="composer-wrap">
    <div class="composer" :data-mode="mode">
      <textarea
        ref="textareaRef"
        v-model="draft"
        class="composer__textarea"
        rows="1"
        :placeholder="meta.placeholder"
        aria-label="消息输入框"
        @input="resize"
        @keydown="handleKeydown"
      ></textarea>

      <div class="composer__footer">
        <ModeSwitch :mode="mode" @change-mode="chooseMode" />

        <div class="composer__footer-right">
          <span class="key-hint">{{ meta.inputHint }}</span>

          <button
            class="send-button"
            :class="{ 'send-button--stop': streaming }"
            type="button"
            :disabled="!streaming && !canSend"
            :aria-label="streaming ? '停止生成' : '发送消息'"
            :title="streaming ? '停止生成' : '发送消息'"
            @click="handleAction"
          >
            <svg v-if="streaming" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <rect x="7" y="7" width="10" height="10" rx="2" fill="currentColor" />
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path
                d="M3.6 20.4 21.3 12 3.6 3.6l2.7 6.8L14.8 12 6.3 13.6l-2.7 6.8Z"
                fill="currentColor"
              />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
