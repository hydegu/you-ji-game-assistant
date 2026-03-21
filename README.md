# YouJiAssistant - 游戏 AI 助手

基于 RAG（检索增强生成）的游戏智能问答系统，支持多游戏知识库隔离、用户偏好自动学习与对话历史管理。

---

## 技术栈

| 组件 | 技术选型 |
|------|---------|
| 后端框架 | Spring Boot 3.5.11 / Java 21 |
| AI 框架 | Spring AI Alibaba 1.1.2.0（Agent Framework） |
| LLM | 通义千问 qwen-plus |
| 向量模型 | text-embedding-v4 (DashScope) |
| 向量数据库 | Milvus v2.5.4（每游戏独立 collection） |
| 关系型数据库 | MySQL + MyBatis Plus 3.5.15 + Druid |
| 缓存 | Redis |
| 认证 | JWT (jjwt 0.12.6) + Spring Security |
| API 文档 | Knife4j 4.5.0 + springdoc-openapi 2.8.0 |
| 连接池 | Druid 1.2.16 |

---

## 系统架构

```
用户提问
  │
  ▼
QueryEnhancementHook          ← 融合用户偏好，增强原始 Query
  │
  ▼
ReactAgent (Spring AI Alibaba)
  ├─ GameTool                 ← 查询游戏元数据（MySQL）
  ├─ DatabaseQueryTool        ← 通用数据库查询（MySQL）
  └─ VectorSearch             ← 语义检索（Milvus，按游戏隔离）
  │
  ▼
AnswerValidationInterceptor   ← 校验回答质量，必要时触发重试
  │
  ▼
PreferenceLearningHook        ← 从对话中提取用户偏好，写入 Redis
  │
  ▼
返回最终答案 + 持久化消息历史（MySQL）
```

---

## 快速开始

### 环境要求

- Java 21+
- Maven 3.8+
- MySQL 5.7+ 或 8.x
- Redis
- Docker & Docker Compose（用于启动 Milvus）

### 1. 启动 Milvus

```bash
docker-compose up -d
```

启动后 Milvus 监听 `localhost:19530`，MinIO 控制台在 `http://localhost:9001`（账密 minioadmin/minioadmin）。

### 2. 初始化数据库

创建数据库并运行建表脚本：

```sql
CREATE DATABASE you_ji_assistant DEFAULT CHARACTER SET utf8mb4;
```

然后执行 `src/main/resources/` 下的 SQL 文件（如有）。

### 3. 配置应用

编辑 `src/main/resources/application.yaml`，修改以下关键配置：

```yaml
spring:
  ai:
    dashscope:
      api-key: <你的 DashScope API Key>
  datasource:
    url: jdbc:mysql://localhost:3306/you_ji_assistant?...
    username: <MySQL 用户名>
    password: <MySQL 密码>
```

其余配置项参见下方[配置说明](#配置说明)。

### 4. 启动应用

```bash
./mvnw spring-boot:run
```

或打包后运行：

```bash
./mvnw package -DskipTests
java -jar target/YouJiAssistant-0.0.1-SNAPSHOT.jar
```

应用默认监听 `http://localhost:8080`。

---

## API 接口文档

启动后访问：

- **Swagger UI**：`http://localhost:8080/swagger-ui.html`
- **Knife4j 增强 UI**：`http://localhost:8080/doc.html`
- **OpenAPI JSON**：`http://localhost:8080/v3/api-docs`

### 接口分组

#### 认证 `/auth`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/login` | 用户登录，返回 JWT 令牌 |
| POST | `/auth/register` | 注册新用户 |

#### AI 对话 `/chat`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/chat/ask` | 向 AI 助手提问（需携带 sessionId 和 gameId） |

#### 会话管理 `/chat/sessions`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/chat/sessions` | 创建新会话，返回 UUID |
| GET | `/chat/sessions` | 获取当前用户的会话列表 |
| GET | `/chat/sessions/{sessionId}/messages` | 获取指定会话的消息历史 |
| DELETE | `/chat/sessions/{sessionId}` | 软删除会话 |

#### 游戏管理 `/api`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/games` | 游戏列表（分页，支持分类/关键字过滤） |
| GET | `/api/games/{id}` | 游戏详情 |
| POST | `/api/games` | 创建游戏 |
| PUT | `/api/games/{id}` | 更新游戏 |
| DELETE | `/api/games/{id}` | 删除游戏 |
| POST | `/api/games/{id}/toggle` | 切换游戏上下架状态 |
| GET | `/api/categories` | 分类列表 |
| GET | `/api/categories/{id}` | 分类详情 |
| POST | `/api/categories` | 创建分类 |
| PUT | `/api/categories/{id}` | 更新分类 |
| DELETE | `/api/categories/{id}` | 删除分类（有游戏时拒绝） |

#### 知识库管理 `/admin/game/{gameId}/documents`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `.../upload` | 上传文档并向量化（最大 50MB） |
| GET | `.../list` | 查询该游戏的文档列表 |
| GET | `.../search` | 向量语义搜索 |
| DELETE | `.../{filename}` | 删除文档及其向量数据 |

> 知识库管理接口需要 ADMIN 角色，对话接口需要 USER 或 ADMIN 角色。

---

## 核心功能说明

### 知识库管理

```
上传文件（PDF/Word/TXT/...）
  → Apache Tika 解析文本内容
  → 按 chunk-size=400 字符、overlap-ratio=15% 切片
  → text-embedding-v4 向量化
  → 存入 Milvus（collection 名称与 gameId 绑定）
  → 记录文档元数据到 MySQL（knowledge_docs 表）
```

### AI 对话（RAG 流程）

```
用户消息 + sessionId + gameId
  → QueryEnhancementHook：读取 Redis 中用户偏好，注入系统提示
  → ReactAgent：多轮 Tool Call
      - VectorSearch：检索对应游戏的 Milvus collection
      - DatabaseQueryTool：查 MySQL 补充结构化数据
      - GameTool：获取游戏基本信息
  → AnswerValidationInterceptor：结构化校验，不合格则重新生成
  → PreferenceLearningHook：异步提取偏好写入 Redis
  → 消息持久化到 chat_messages 表
```

### 会话管理

- 会话 ID 由客户端首次调用 `POST /chat/sessions` 获取（UUID）
- 实际 `chat_session` 记录在第一次发消息时惰性创建，避免空会话
- 软删除：仅设置 `deleted_at`，底层 AI checkpoint 数据保留
- 权限隔离：每个用户只能访问自己的会话

### 用户偏好学习

- `PreferenceLearningHook` 在每次对话结束后运行
- LLM 从对话内容中提取结构化偏好（`UserGamePreference` POJO）
- 偏好存储在 Redis，Key 格式：`user:{userId}:game:{gameId}:preference`
- `QueryEnhancementHook` 在下次对话前读取偏好并注入提示词

---

## 项目结构

```
src/main/java/com/example/assistant/
├── config/          # Spring 配置（AgentConfig、MybatisPlusConfig 等）
├── controller/      # REST 控制器（Auth/Chat/Session/Game/Document）
├── service/         # 业务逻辑接口及实现
├── entity/          # MyBatis Plus 实体（User、Game、ChatSession 等）
├── mapper/          # MyBatis Plus Mapper 接口
├── dto/             # 请求/响应 DTO
├── hooks/           # AI Agent Hook（QueryEnhancement、PreferenceLearning）
├── intercepter/     # AI Agent Interceptor（AnswerValidation）
├── tools/           # Agent 工具（GameTool、DatabaseQueryTool）
├── component/       # 公共组件（GameVectorStoreFactory、UserPreferenceStore）
├── security/        # JWT + Spring Security 配置
├── pojo/            # LLM 结构化输出 POJO（UserGamePreference 等）
├── constant/        # 常量（Prompts、DataBase）
├── exception/       # 异常体系及全局处理器
└── utils/           # 工具类（SecurityUtils）

src/main/resources/
├── application.yaml # 主配置文件
└── mapper/          # MyBatis XML（如有）

docker-compose.yml   # Milvus + MinIO + etcd
pom.xml
```

---

## 配置说明

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `server.port` | `8080` | 服务端口 |
| `spring.ai.dashscope.api-key` | — | DashScope API Key（**必填**） |
| `spring.ai.dashscope.chat.options.model` | `qwen-plus` | 对话模型 |
| `spring.ai.dashscope.embedding.options.model` | `text-embedding-v4` | 向量模型 |
| `spring.ai.vectorstore.milvus.client.host` | `localhost` | Milvus 地址 |
| `spring.ai.vectorstore.milvus.client.port` | `19530` | Milvus 端口 |
| `spring.datasource.url` | `localhost:3306/you_ji_assistant` | MySQL 连接 |
| `app.jwt.secret` | — | JWT 签名密钥（生产环境需替换） |
| `app.jwt.expiration` | `86400000` | JWT 有效期（毫秒，默认 24 小时） |
| `game.assistant.chunk-size` | `400` | 文档分片字符数 |
| `game.assistant.overlap-ratio` | `0.15` | 分片重叠比例 |
| `spring.servlet.multipart.max-file-size` | `100MB` | 单文件上传上限 |
| `file.storage.local.base-path` | `./data/upload/files` | 本地文件存储路径 |
