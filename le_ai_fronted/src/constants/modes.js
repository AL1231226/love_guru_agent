export const MODE_LOVE = 'love'
export const MODE_AGENT = 'agent'

export const MODE_META = {
  [MODE_LOVE]: {
    id: MODE_LOVE,
    label: '会话模式',
    shortLabel: '会话',
    assistantName: '恋爱大师',
    tagline: '懂倾听、会给建议的 AI 情感顾问',
    emptyTitle: '今天想聊点什么？',
    emptyText: '把你的情感困惑告诉恋爱大师，我会帮你理清思路，并给出可以马上行动的建议。',
    emptyNote: '同一会话内会保留上下文，点击「新对话」即可重新开始。',
    placeholder: '说说你的情感困惑…',
    inputHint: 'Enter 发送 · Shift + Enter 换行',
    suggestions: [
      '怎么自然地开启一段对话？',
      '帮我把这段聊天记录分析一下',
      '写一封真诚的表白信',
      '暧昧期应该如何推进关系？',
    ],
  },
  [MODE_AGENT]: {
    id: MODE_AGENT,
    label: 'Agent 模式',
    shortLabel: 'Agent',
    assistantName: 'LeManus',
    tagline: '会调用工具、分步执行任务的智能体',
    emptyTitle: '交给 LeManus 来办',
    emptyText: '描述一个任务，Agent 会拆解步骤、调用工具，并把每一步的执行结果汇报给你。',
    emptyNote: 'Agent 每次任务独立执行，不保留历史上下文。',
    placeholder: '描述你想让 Agent 完成的任务…',
    inputHint: 'Enter 发送 · Shift + Enter 换行',
    suggestions: [
      '规划一场周末约会，并给出预算',
      '帮我查找适合约会的餐厅',
      '为纪念日写一份完整的策划案',
      '制定一个 30 天的恋爱提升计划',
    ],
  },
}
