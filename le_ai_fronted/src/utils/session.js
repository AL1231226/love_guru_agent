export function createChatId() {
  const cryptoObj = globalThis.crypto

  if (cryptoObj?.randomUUID) {
    try {
      return cryptoObj.randomUUID()
    } catch {
      // 某些非安全上下文会抛错，降级到随机字符串
    }
  }

  const randomPart = () => Math.random().toString(36).slice(2, 10)
  return `chat-${Date.now().toString(36)}-${randomPart()}${randomPart()}`
}
