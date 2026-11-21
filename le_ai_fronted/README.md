# 恋爱大师 · AI 对话前端

基于 **Vue 3 + Axios** 的单页聊天应用，对接后端两个 SSE 流式接口：

| 模式 | 接口 | 参数 |
| --- | --- | --- |
| 会话模式 | `GET /api/ai/love_app/chat/sse/emitter` | `message`、`chatId` |
| Agent 模式 | `GET /api/ai/manus/chat` | `message` |

## 功能

- 单页聊天界面，用户与 AI 均有头像，AI 头像随模式切换（❤️ / 🤖）。
- 输入框左下角切换「会话模式 / Agent 模式」，切换后自动开启新对话。
- 进入页面自动生成会话 ID；顶部展示并支持一键复制；点击「新对话」重新生成。
- Axios（XHR）读取 SSE 流，回复逐字增量渲染，支持 Markdown（markdown-it + DOMPurify）。
- 支持停止生成、自动滚动、回到底部、空状态推荐问题、错误提示与移动端适配。

## 环境要求

- Node.js >= 20.19
- 后端已启动在 `http://localhost:8123`（`server.port: 8123`，`context-path: /api`）

## 启动

```bash
cd le_ai_fronted
npm install
npm run dev
```

打开 `http://localhost:5173`。开发服务器会把 `/api` 代理到 `http://localhost:8123`。

如需修改后端地址，复制 `.env.example` 为 `.env.local` 并调整：

```bash
# 代理目标（不带 /api）
VITE_PROXY_TARGET=http://localhost:8123

# 或跳过代理直接请求后端（需带 /api）
# VITE_API_BASE_URL=http://localhost:8123/api
```

## 构建与测试

```bash
npm run build   # 生产构建，产物在 dist/
npm run preview # 本地预览构建产物
npm test        # SSE 增量解析器单元测试（Node 内置 test runner）
```

## 目录结构

```
src/
├── api/chat.js              # Axios 实例、两种模式的流式请求
├── composables/useChat.js   # 消息状态、发送/停止/切换模式/新建会话
├── components/
│   ├── ChatHeader.vue       # 标题、会话 ID、复制、新对话
│   ├── ChatMessageList.vue  # 消息列表与滚动控制
│   ├── ChatMessageItem.vue  # 气泡、Markdown、错误提示
│   ├── ChatComposer.vue     # 输入框、发送/停止、模式切换位置
│   ├── ModeSwitch.vue       # 会话模式 / Agent 模式
│   ├── EmptyState.vue       # 空状态与推荐问题
│   └── ChatAvatar.vue       # 用户与 AI 头像
├── utils/sseParser.js       # 增量 SSE 解析器
├── utils/markdown.js        # Markdown 渲染与 XSS 过滤
├── utils/session.js         # 会话 ID 生成
└── styles/main.css          # 设计令牌与全部样式
```

## 实现说明

- **流式读取**：使用 Axios 的 `onDownloadProgress` 监听 XHR 分片，对比
  `responseText` 的累计长度做增量解析，不依赖 `EventSource`。
- **会话 ID**：优先使用 `crypto.randomUUID()`，非安全上下文下降级为时间戳 + 随机串。
- **切换模式**：会中止正在生成的请求并清空当前消息，同时生成新的会话 ID；Agent
  模式每次任务独立执行，不保留上下文。
- **安全**：Markdown 关闭原始 HTML，渲染结果再经 DOMPurify 过滤，外部链接自动
  `target="_blank"` 并附带 `rel="noopener noreferrer"`。

## 已知限制

后端通过 Spring `SseEmitter` 直接发送纯文本（`data:<文本>\n\n`），没有对文本中的
换行做转义。SSE 协议会把换行当作事件分隔符，因此**位于 chunk 末尾的换行可能丢失**，
影响部分 Markdown 段落/列表的还原。前端解析器已尽量兼容 chunk 内部的换行；如需完全
保真，建议后端改为发送 JSON 字符串（例如 `JSON.stringify({ content })`），前端再对
`data:` 内容做 `JSON.parse`。
