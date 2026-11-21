/**
 * 增量式 SSE（Server-Sent Events）解析器。
 *
 * 后端的 Spring `SseEmitter` 以 `data:<payload>\n\n` 的形式发送字符串，
 * 但并不会转义 payload 内部换行。严格按 SSE 规范解析会丢弃内部换行之后的
 * 内容，因此这里额外把无法识别的行当作 data 续行处理，并用 "\n" 重新拼接。
 *
 * 用法：
 *   const parser = createSseParser((data) => { ... })
 *   parser.feed(textChunk)
 *   parser.flush() // 流结束时调用
 */
const EVENT_BOUNDARY = /\r\n\r\n|\n\n|\r\r/
const FIELD_PREFIX = /^(event|id|retry):/

function parseBlock(rawBlock) {
  // 连续的事件分隔符来自 payload 中的换行 token（SseEmitter 未转义换行），
  // 每个空事件块还原为一个换行符。
  if (rawBlock === '') return '\n'

  const lines = rawBlock.split(/\r\n|\r|\n/)
  const parts = []
  let hasDataField = false

  for (const line of lines) {
    if (line === '' || line.startsWith(':')) continue

    if (line.startsWith('data:')) {
      hasDataField = true
      let value = line.slice(5)
      // 按 SSE 规范，冒号后允许一个可选空格
      if (value.startsWith(' ')) value = value.slice(1)
      parts.push(value)
      continue
    }

    // event / id / retry 是 SSE 控制字段，不属于正文
    if (FIELD_PREFIX.test(line)) continue

    // 兼容 SseEmitter 未转义的原始换行
    parts.push(line)
  }

  if (!hasDataField && parts.length === 0) return null

  const payload = parts.join('\n')
  // 纯换行 token 在 `data:\n\n` 中会变成空 payload，这里还原为换行符
  return hasDataField && payload === '' ? '\n' : payload
}

export function createSseParser(onData) {
  if (typeof onData !== 'function') {
    throw new TypeError('createSseParser 需要一个 onData 回调')
  }

  let buffer = ''
  let closed = false

  function dispatch(rawBlock) {
    const payload = parseBlock(rawBlock)
    if (payload !== null) onData(payload)
  }

  return {
    feed(chunk) {
      if (closed || typeof chunk !== 'string' || chunk === '') return

      buffer += chunk

      let match
      while ((match = EVENT_BOUNDARY.exec(buffer))) {
        const rawBlock = buffer.slice(0, match.index)
        buffer = buffer.slice(match.index + match[0].length)
        dispatch(rawBlock)
      }
    },

    flush() {
      if (closed) return
      closed = true
      if (buffer !== '') dispatch(buffer)
      buffer = ''
    },
  }
}
