<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import ChatMessageItem from './ChatMessageItem.vue'
import EmptyState from './EmptyState.vue'

const props = defineProps({
  messages: {
    type: Array,
    required: true,
  },
  mode: {
    type: String,
    required: true,
  },
})

defineEmits(['suggest'])

const scrollRef = ref(null)
const nearBottom = ref(true)
const showJump = computed(() => !nearBottom.value)

const signature = computed(() =>
  props.messages.map((message) => `${message.id}:${message.status}:${message.content.length}`).join('|'),
)

function scrollToBottom(behavior = 'auto') {
  const el = scrollRef.value
  if (!el) return

  if (behavior === 'smooth') {
    el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' })
  } else {
    el.scrollTop = el.scrollHeight
  }
}

function handleScroll() {
  const el = scrollRef.value
  if (!el) return
  nearBottom.value = el.scrollHeight - el.scrollTop - el.clientHeight < 96
}

async function jumpToBottom() {
  nearBottom.value = true
  await nextTick()
  scrollToBottom('smooth')
}

watch(
  () => props.messages.length,
  async (count) => {
    if (count === 0) {
      nearBottom.value = true
      return
    }

    nearBottom.value = true
    await nextTick()
    scrollToBottom('smooth')
  },
)

watch(
  signature,
  async () => {
    if (!nearBottom.value) return
    await nextTick()
    scrollToBottom('auto')
  },
  { flush: 'post' },
)

onMounted(async () => {
  await nextTick()
  scrollToBottom('auto')
})
</script>

<template>
  <div class="message-list-wrap">
    <div
      ref="scrollRef"
      class="message-list"
      role="log"
      aria-label="对话记录"
      @scroll.passive="handleScroll"
    >
      <div class="message-list__inner">
        <EmptyState v-if="messages.length === 0" :mode="mode" @suggest="$emit('suggest', $event)" />

        <TransitionGroup v-else name="message" tag="div" class="message-stack" appear>
          <ChatMessageItem
            v-for="message in messages"
            :key="message.id"
            :message="message"
            :mode="mode"
          />
        </TransitionGroup>
      </div>
    </div>

    <Transition name="fade">
      <button
        v-if="showJump"
        class="jump-button"
        type="button"
        aria-label="回到对话底部"
        title="回到最新消息"
        @click="jumpToBottom"
      >
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path
            d="M12 5v13m-5.2-5.2L12 18l5.2-5.2"
            stroke="currentColor"
            stroke-width="1.8"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </button>
    </Transition>
  </div>
</template>
