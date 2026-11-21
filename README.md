# 恋爱大师 AI Agent

基于 **Spring AI + Spring AI Alibaba** 的 AI 应用实战项目：一个具备 RAG 知识库、多轮记忆和工具调用能力的「恋爱顾问」智能体，附带 ReAct Agent 模式、MCP Server 以及 Vue 3 聊天前端。

## 功能特性

- **恋爱顾问对话**：接入 DashScope 云知识库做 RAG 检索增强，覆盖单身 / 恋爱 / 已婚三种场景，支持同步与 SSE 流式输出
- **多轮会话记忆**：按 `chatId` 隔离上下文，Kryo 序列化持久化到本地 `chat-memory/`
- **Agent 模式（LeManus）**：ReAct 风格的自主智能体，可自主选择工具、分步完成任务，最多 20 步
- **内置工具**：文件读写、联网搜索、网页抓取、资源下载、终端命令、PDF 生成
- **MCP 支持**：主应用可作为 MCP 客户端；`image-search-mcp-server` 提供天气与火车票查询工具，支持 stdio / SSE 两种模式
- **聊天前端**：Vue 3 + Vite，SSE 增量渲染、Markdown 展示、会话 / Agent 模式切换

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 后端 | Java 21、Spring Boot 3.4、Spring AI 1.0、Spring AI Alibaba 1.0.0.4 |
| 大模型 | 阿里云百炼 DashScope（Qwen） |
| 存储 | 本地文件（Kryo 会话记忆）、PostgreSQL + pgvector（可选，配置已注释） |
| 协议 | REST、SSE、MCP |
| 前端 | Vue 3、Vite 8、Axios、markdown-it |
| 文档 | Knife4j / OpenAPI 3 |

## 项目结构

```
le_ai_agent/
├── src/main/java/com/liule/le_ai_agent/
│   ├── app/          # LoveApp：恋爱顾问（RAG + 会话记忆）
│   ├── agent/        # ReAct 智能体 LeManus
│   ├── advisor/      # 自定义 Advisor（RAG / 日志）
│   ├── chatmemory/   # Kryo 文件会话记忆
│   ├── controller/   # REST / SSE 接口
│   ├── rag/          # 知识库加载与检索
│   ├── tools/        # 工具集
│   └── config/       # 跨域、MCP 等配置
├── le_ai_fronted/            # Vue 3 聊天前端
├── image-search-mcp-server/  # MCP Server（天气 / 火车票）
└── src/main/resources/
    ├── document/       # 恋爱问答知识库（Markdown）
    └── mcp/            # MCP 客户端配置
```

## 接口一览

后端 `context-path` 为 `/api`，默认端口 `8123`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/ai/love_app/chat/sync?message=&chatId=` | 恋爱顾问同步对话 |
| GET | `/api/ai/love_app/chat/sse/emitter?message=&chatId=` | 恋爱顾问 SSE 流式对话 |
| GET | `/api/ai/manus/chat?message=` | Agent 模式流式对话 |
| GET | `/api/health` | 健康检查 |
| GET | `/api/swagger-ui.html` | Knife4j 接口文档 |

## 快速开始

### 环境要求

- JDK 21+、Maven 3.9+
- Node.js 20.19+
- 阿里云百炼 API Key（**需要在百炼控制台创建名为 `恋爱大师` 的知识库索引**，或在 `LoveAppRagCloudAdvisorConfig` 中改成你的索引名）
- SearchAPI Key（联网搜索工具使用，可选）

### 1. 配置密钥

后端通过环境变量读取 DashScope Key：

```bash
# Linux / macOS / Git Bash
export qiwen_key="sk-你的key"

# Windows CMD
setx qiwen_key "sk-你的key"
```

联网搜索配置在 `application-prod.yaml`（已被 `.gitignore` 排除，需本地自建）：

```yaml
searchapi:
  api-key: ${SEARCHAPI_KEY}
  engine: baidu
```

> `application.yaml` 中 `spring.ai.dashscope.base-url` 指向的是自建 MaaS 端点；如果使用百炼公共端点，删除该行即可。模型名可通过 `spring.ai.dashscope.chat.options.model` 修改。

### 2. 启动后端

```bash
./mvnw spring-boot:run
```

启动后访问：

- 健康检查：http://localhost:8123/api/health
- 接口文档：http://localhost:8123/api/swagger-ui.html

### 3. 启动前端

```bash
cd le_ai_fronted
npm install
npm run dev
```

打开 http://localhost:5173 ，开发服务器会把 `/api` 代理到 `http://localhost:8123`。如需修改后端地址，复制 `.env.example` 为 `.env.local` 并设置 `VITE_PROXY_TARGET`。

### 4. （可选）启动 MCP Server

```bash
cd image-search-mcp-server
./mvnw spring-boot:run -Dspring-boot.run.profiles=stdio   # stdio 模式，供主应用以子进程接入
./mvnw spring-boot:run                                    # SSE 模式，默认端口 8127
```

天气接口使用 `uapi.api-key`（`application.yaml` 中配置）。主应用接入方式见 `src/main/resources/mcp/mcp-servers.json`，启用 `application.yaml` 中被注释的 `spring.ai.mcp.client` 配置即可。

## 生产部署

### 后端

```bash
./mvnw clean package -DskipTests
java -jar target/le_ai_agent-0.0.1-SNAPSHOT.jar
```

建议用 systemd 或容器托管，并确保环境变量 `qiwen_key`、`SEARCHAPI_KEY` 已注入。

### 前端

```bash
cd le_ai_fronted
npm ci
npm run build        # 产物在 dist/
```

将 `dist/` 交给 Nginx 托管，并把 `/api` 反向代理到后端（**必须关闭缓冲，否则 SSE 无法流式输出**）：

```nginx
server {
    listen 80;
    root /var/www/love-guru/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8123/api/;
        proxy_http_version 1.1;
        proxy_set_header Connection "";
        proxy_buffering off;
        proxy_cache off;
        proxy_read_timeout 180s;
    }
}
```

### MCP Server（可选）

```bash
cd image-search-mcp-server
./mvnw clean package -DskipTests
java -jar target/image-search-mcp-server-0.0.1-SNAPSHOT.jar
```

## 注意事项

- 不要提交真实密钥：`application-prod.yaml`、`.env*`、`chat-memory/`、`tmp/` 已在 `.gitignore` 中排除
- `chat-memory/` 存放会话序列化文件，可能包含聊天内容，请勿公开
- 当前 CORS 允许所有来源，生产环境建议收紧 `CorsConfig`
- pgvector 向量库配置目前为注释状态，启用需准备 PostgreSQL 并安装 pgvector 扩展

## License

仅用于学习与交流。
