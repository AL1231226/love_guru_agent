import axios from 'axios'
import { MODE_AGENT, MODE_LOVE } from '../constants/modes'
import { createSseParser } from '../utils/sseParser'

const ENDPOINTS = {
  [MODE_LOVE]: '/ai/love_app/chat/sse/emitter',
  [MODE_AGENT]: '/ai/manus/chat',
}

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 0,
})

export function isAbortError(error) {
  return (
    error?.code === 'ERR_CANCELED' ||
    error?.name === 'CanceledError' ||
    error?.name === 'AbortError'
  )
}

/**
 * 通过 Axios(XHR) 读取 SSE 流。
 *
 * `onDownloadProgress` 会在浏览器收到新分片时触发，我们利用 `responseText`
 * 的累计长度做增量解析，从而避免使用 EventSource（无法满足 Axios 技术选型）。
 */
export async function streamChat({ mode, message, chatId, signal, onData }) {
  const url = ENDPOINTS[mode]
  if (!url) throw new Error(`未知的对话模式：${mode}`)

  const parser = createSseParser(onData)
  let consumed = 0

  const feed = (text) => {
    if (typeof text !== 'string' || text.length <= consumed) return
    parser.feed(text.slice(consumed))
    consumed = text.length
  }

  try {
    const response = await http.get(url, {
      params: mode === MODE_AGENT ? { message } : { message, chatId },
      responseType: 'text',
      adapter: 'xhr',
      signal,
      timeout: 0,
      headers: { Accept: 'text/event-stream' },
      transformResponse: [(data) => data],
      onDownloadProgress: (event) => {
        const xhr = event?.event?.target
        if (!xhr) return
        if (xhr.status && (xhr.status < 200 || xhr.status >= 300)) return
        feed(xhr.responseText)
      },
    })

    // 流结束时兜底读取一次完整响应
    feed(typeof response.data === 'string' ? response.data : '')
    parser.flush()

    return response
  } catch (error) {
    // 保留已经收到的部分内容，错误由上层统一展示
    parser.flush()
    throw error
  }
}
