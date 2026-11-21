<script setup>
import { onBeforeUnmount } from 'vue'
import ChatComposer from './components/ChatComposer.vue'
import ChatHeader from './components/ChatHeader.vue'
import ChatMessageList from './components/ChatMessageList.vue'
import { useChat } from './composables/useChat'

const {
  messages,
  mode,
  chatId,
  isStreaming,
  sendMessage,
  stopStreaming,
  switchMode,
  newConversation,
} = useChat()

onBeforeUnmount(stopStreaming)
</script>

<template>
  <div class="app-shell">
    <div class="ambient ambient--rose" aria-hidden="true"></div>
    <div class="ambient ambient--violet" aria-hidden="true"></div>

    <main class="chat-card">
      <ChatHeader
        :mode="mode"
        :chat-id="chatId"
        :streaming="isStreaming"
        @new-chat="newConversation"
      />

      <ChatMessageList :messages="messages" :mode="mode" @suggest="sendMessage" />

      <ChatComposer
        :mode="mode"
        :streaming="isStreaming"
        @send="sendMessage"
        @stop="stopStreaming"
        @change-mode="switchMode"
      />

      <p class="sr-only" role="status" aria-live="polite">
        {{ isStreaming ? 'AI 正在回复' : '' }}
      </p>
    </main>
  </div>
</template>
