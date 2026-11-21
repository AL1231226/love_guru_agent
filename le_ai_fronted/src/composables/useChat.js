import { ref } from 'vue'
import { isAbortError, streamChat } from '../api/chat'
import { MODE_AGENT, MODE_LOVE } from '../constants/modes'
import { createChatId } from '../utils/session'

let messageCounter = 0

function createMessage(role, mode, content, status) {
  messageCounter += 1
  return {
    id: `${role}-${Date.now()}-${messageCounter}`,
    role,
    mode,
    content,
    status,
    error: '',
    createdAt: Date.now(),
  }
}

function describeError(error) {
  if (error?.response) {
    const { status, data } = error.response
    if (typeof data === 'string' && data.trim()) {
      return `请求失败（HTTP ${status}）：${data.trim().slice(0, 200)}`
    }
    return `请求失败（HTTP ${status}）`
  }

  if (error?.message === 'Network Error') {
    return '网络连接失败：无法连接后端服务，请确认服务已启动。'
  }

  return error?.message ? `请求失败：${error.message}` : '请求失败：未知错误'
}

export function useChat() {
  const messages = ref([])
  const mode = ref(MODE_LOVE)
  const chatId = ref(createChatId())
  const isStreaming = ref(false)

  let activeController = null

  function stopStreaming() {
    activeController?.abort()
  }

  function newConversation() {
    stopStreaming()
    messages.value = []
    chatId.value = createChatId()
  }

  function switchMode(nextMode) {
    if (nextMode !== MODE_LOVE && nextMode !== MODE_AGENT) return
    if (nextMode === mode.value) return

    stopStreaming()
    messages.value = []
    mode.value = nextMode
    chatId.value = createChatId()
  }

  async function sendMessage(rawText) {
    const text = String(rawText ?? '').trim()
    if (!text || isStreaming.value) return

    const requestMode = mode.value
    const requestChatId = chatId.value
    const controller = new AbortController()
    activeController = controller

    messages.value.push(
      createMessage('user', requestMode, text, 'done'),
      createMessage('assistant', requestMode, '', 'streaming'),
    )
    // 必须拿 ref 里的响应式代理，直接改原始对象不会触发视图更新
    const assistant = messages.value[messages.value.length - 1]

    isStreaming.value = true
    let receivedEvent = false

    try {
      await streamChat({
        mode: requestMode,
        message: text,
        chatId: requestChatId,
        signal: controller.signal,
        onData: (chunk) => {
          if (!chunk || assistant.status !== 'streaming') return

          // Agent 每个事件是一条完整的步骤结果，步骤之间补一个空行
          if (receivedEvent && requestMode === MODE_AGENT) {
            assistant.content += '\n\n'
          }

          assistant.content += chunk
          receivedEvent = true
        },
      })

      if (assistant.status === 'streaming') {
        assistant.status = 'done'
        if (!assistant.content.trim()) {
          assistant.content = '（本次没有收到回复内容）'
        }
      }
    } catch (error) {
      if (isAbortError(error)) {
        if (assistant.status === 'streaming') {
          assistant.status = 'stopped'
          if (!assistant.content.trim()) {
            assistant.content = '已停止生成'
          }
        }
      } else {
        assistant.status = 'error'
        assistant.error = describeError(error)
      }
    } finally {
      if (activeController === controller) {
        activeController = null
        isStreaming.value = false
      }
    }
  }

  return {
    messages,
    mode,
    chatId,
    isStreaming,
    sendMessage,
    stopStreaming,
    switchMode,
    newConversation,
  }
}
