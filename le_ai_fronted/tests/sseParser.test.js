import assert from 'node:assert/strict'
import test from 'node:test'
import { createSseParser } from '../src/utils/sseParser.js'

function setup() {
  const chunks = []
  const parser = createSseParser((data) => chunks.push(data))
  return { chunks, parser }
}

test('解析单个 SSE 事件', () => {
  const { chunks, parser } = setup()
  parser.feed('data:hello\n\n')
  assert.deepEqual(chunks, ['hello'])
})

test('跨 chunk 的 UTF-8 与事件边界', () => {
  const { chunks, parser } = setup()
  parser.feed('da')
  parser.feed('ta:你')
  parser.feed('好\n')
  parser.feed('\n')
  assert.deepEqual(chunks, ['你好'])
})

test('一次 feed 中包含多个事件', () => {
  const { chunks, parser } = setup()
  parser.feed('data:第一段\n\ndata:第二段\n\n')
  assert.deepEqual(chunks, ['第一段', '第二段'])
})

test('支持标准的多行 data 字段', () => {
  const { chunks, parser } = setup()
  parser.feed('data:line1\ndata:line2\n\n')
  assert.deepEqual(chunks, ['line1\nline2'])
})

test('兼容 SseEmitter 未转义的原始换行', () => {
  const { chunks, parser } = setup()
  parser.feed('data:line1\nline2\n\n')
  assert.deepEqual(chunks, ['line1\nline2'])
})

test('兼容跨 chunk 拆开的 CRLF 边界', () => {
  const { chunks, parser } = setup()
  parser.feed('data:a\r')
  parser.feed('\n\r\n')
  assert.deepEqual(chunks, ['a'])
})

test('忽略注释、event 与 id 控制字段', () => {
  const { chunks, parser } = setup()
  parser.feed(': keep-alive\n\n')
  parser.feed('event: message\nid: 42\ndata:x\n\n')
  assert.deepEqual(chunks, ['x'])
})

test('data 冒号后的一个可选空格会被去掉', () => {
  const { chunks, parser } = setup()
  parser.feed('data:  value\n\n')
  assert.deepEqual(chunks, [' value'])
})

test('flush 会处理没有终止边界的尾部数据', () => {
  const { chunks, parser } = setup()
  parser.feed('data:tail')
  assert.deepEqual(chunks, [])
  parser.flush()
  assert.deepEqual(chunks, ['tail'])
})

test('纯换行 token 会被还原为换行符', () => {
  const { chunks, parser } = setup()
  parser.feed('data:\n\n\n')
  parser.flush()
  assert.deepEqual(chunks, ['\n'])
})

test('连续换行 token 会被还原为多个换行符', () => {
  const { chunks, parser } = setup()
  parser.feed('data:\n\n\n\n')
  parser.flush()
  assert.deepEqual(chunks, ['\n', '\n'])
})

test('文本后的换行 token 会被还原', () => {
  const { chunks, parser } = setup()
  parser.feed('data:第一行\n\n\n\ndata:第二行\n\n')
  parser.flush()
  assert.deepEqual(chunks, ['第一行', '\n', '第二行'])
})

test('flush 之后不再接受新的数据', () => {
  const { chunks, parser } = setup()
  parser.feed('data:a\n\n')
  parser.flush()
  parser.feed('data:b\n\n')
  assert.deepEqual(chunks, ['a'])
})
