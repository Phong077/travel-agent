# 智能旅行规划助手

基于 Spring Boot、Spring AI Alibaba 和阿里百炼构建的智能旅行规划助手。项目支持根据用户的出发地、目的地、天数、人数、预算、偏好和避免项生成结构化旅行计划，并结合本地四川旅行知识库进行轻量级 RAG 检索增强。

## 核心功能

- 结构化旅行计划生成：按天返回主题、上午、下午、晚上和交通建议。
- 轻量级 RAG：读取本地 Markdown 知识库，基于加权关键词召回 TopK 参考资料。
- 可插拔目的地知识库：按 `common + destination` 目录加载资料，支持四川、云南等目的地扩展。
- 引用来源返回：接口返回 references，展示本次生成参考了哪些知识片段。
- 预算分析：根据总预算、人数和天数计算人均预算、每日预算和预算等级。
- 避免项约束：支持避免太早起床、频繁换酒店等用户限制。
- 统一响应与异常处理：成功和失败响应格式统一，便于前端处理。
- 调试日志：输出检索关键词、命中知识、预算分析和 AI 原始返回。
- 前端控制台：提供行程生成、知识库调试、引用来源、每日详情和结果复制等完整演示流程。

## 技术栈

后端：

- Java 17
- Spring Boot 3.5.x
- Spring AI Alibaba
- 阿里百炼 DashScope
- Maven
- Jackson
- 本地 Markdown 知识库

前端：

- Vue 3
- TypeScript
- Vite
- Vue Router
- pnpm
- Stitch 高保真原型

## 项目结构

```text
travel-agent
├─ frontend                 # Vue 3 前端
├─ scripts                  # Stitch 原型下载脚本
├─ stitch-export            # Stitch 导出的原型 HTML 与截图
├─ src/main/resources/knowledge
│  ├─ common                # 通用旅行规则
│  ├─ sichuan               # 四川目的地知识库
│  └─ yunnan                # 云南目的地知识库
└─ src/main/java/com/example/travelagent
```

后端核心包结构：

```text
src/main/java/com/example/travelagent
├─ api
│  ├─ TripController.java
│  ├─ KnowledgeController.java
│  ├─ ApiResponse.java
│  └─ GlobalExceptionHandler.java
├─ application
│  ├─ TravelPlanningService.java
│  ├─ BudgetService.java
│  └─ AiResponseParseException.java
├─ domain
│  ├─ PlanTripRequest.java
│  ├─ TripPlanResponse.java
│  ├─ ItineraryDay.java
│  ├─ BudgetAnalysis.java
│  └─ KnowledgeReference.java
└─ knowledge
   ├─ KnowledgeBaseLoader.java
   ├─ KnowledgeRetrievalService.java
   ├─ KnowledgeDocument.java
   └─ KnowledgeSearchResult.java
```

## 后端运行方式

先配置阿里百炼 API Key，不要把 Key 写进配置文件。

PowerShell:

```powershell
$env:DASHSCOPE_API_KEY="你的阿里百炼Key"
```

或者在 IDEA 的 Run Configuration 中配置环境变量：

```text
DASHSCOPE_API_KEY=你的阿里百炼Key
```

启动项目：

```bash
mvn spring-boot:run
```

默认端口：

```text
8092
```

## 前端运行方式

进入前端目录：

```bash
cd frontend
```

安装依赖：

```bash
pnpm install
```

启动前端：

```bash
pnpm dev
```

访问：

```text
http://localhost:5173
```

前端会通过 Vite 代理访问后端：

```text
/api -> http://localhost:8092
```

如果后端未启动，前端会自动使用 mock 数据兜底，并在页面顶部提示当前为演示数据。

常用命令：

```bash
pnpm dev
pnpm build
pnpm preview
```

## 前端页面

```text
/             控制台首页，动态展示最近一次行程状态
/plan         旅行规划输入表单，支持自定义偏好和避坑项
/loading      生成中状态，展示预算分析、知识库检索、行程生成过程
/result       旅行计划概览，展示摘要、预算分析、知识库来源和复制摘要
/days         每日行程详情，支持复制单日安排
/references   RAG 引用来源，展示引用片段、匹配分数和知识库标签
/knowledge    知识库检索调试台，展示请求 JSON、响应 JSON 和命中文档
```

前端已实现的工程化处理：

- API 请求封装：统一处理 JSON 请求、业务响应、HTTP 错误和超时。
- 接口超时控制：行程生成 45 秒超时，知识库检索 12 秒超时。
- 真实接口 / Mock 兜底状态提示：后端可用时显示真实接口连接状态，后端不可用时展示 mock 原因。
- 动态 Mock：后端不可用时，mock 行程和 mock 检索结果会根据用户输入的目的地、天数、预算、偏好动态生成。
- 本地缓存：使用 `sessionStorage` 保存最近一次请求和结果，刷新后仍可查看。
- 缓存容错：请求参数和行程结果会做结构标准化，避免坏缓存导致页面异常。
- 空状态：未生成行程时，结果页、每日详情页和引用页会引导用户先新建行程。
- 复制能力：支持复制完整行程摘要、单日行程、单条引用、知识库请求 JSON 和响应 JSON。

## 前端演示流程

1. 启动后端，确保 `http://localhost:8092` 可访问。
2. 启动前端，打开 `http://localhost:5173`。
3. 在 `/plan` 输入目的地，例如 `四川`、`云南` 或未知目的地。
4. 添加旅行偏好和避坑项，点击生成 AI 行程。
5. 在 `/result` 查看预算分析、知识库来源和行程摘要。
6. 在 `/days` 查看每日安排，并复制单日行程。
7. 在 `/references` 查看 RAG 引用来源和知识库标签。
8. 在 `/knowledge` 调试检索链路，展示请求体 JSON、响应体 JSON 和命中文档。

## 接口示例

### 生成旅行计划

```http
POST http://localhost:8092/api/trips/plan
Content-Type: application/json
```

请求体：

```json
{
  "departureCity": "重庆",
  "destination": "四川",
  "days": 5,
  "travelers": 2,
  "budget": 6000,
  "preferences": ["美食", "自然风景", "轻松节奏"],
  "avoid": ["太早起床", "每天换酒店"]
}
```

响应示例：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "destination": "四川",
    "totalDays": 5,
    "summary": "以成都为基地，兼顾美食、自然和轻松节奏...",
    "days": [
      {
        "day": 1,
        "theme": "成都初体验",
        "morning": "从重庆出发抵达成都，入住酒店后休整。",
        "afternoon": "城市漫步，体验茶馆和街区文化。",
        "evening": "品尝成都小吃，避免过度奔波。",
        "transportTip": "以高铁和市内地铁为主。"
      }
    ],
    "references": [
      {
        "title": "成都",
        "source": "sichuan-attractions.md",
        "snippet": "成都适合作为大多数四川旅行的起点...",
        "score": 3
      }
    ],
    "budgetAnalysis": {
      "perPersonBudget": 3000,
      "perPersonDailyBudget": 600,
      "level": "适中",
      "suggestion": "预算适中，可以兼顾成都城市体验、周边景点和部分特色美食，建议控制长距离路线数量。"
    }
  }
}
```

### 检索知识库

```http
POST http://localhost:8092/api/knowledge/search
Content-Type: application/json
```

该接口只返回 RAG 检索命中的知识片段，不调用大模型，主要用于调试检索效果。

## RAG 流程

```text
用户请求
  -> DestinationResolver 解析目的地知识库 key
  -> 加载 common + 当前目的地知识库
  -> 提取目的地、偏好等关键词
  -> 在当前知识库范围内加权关键词检索
  -> 返回 TopK references
  -> 将 references 注入 Prompt
  -> 调用阿里百炼生成结构化 JSON
  -> Jackson 解析为 TripPlanResponse
  -> 返回旅行计划、预算分析和引用来源
```

## 可插拔目的地知识库

知识库按目录组织：

```text
src/main/resources/knowledge/
├─ common/
│  └─ travel-rules.md
├─ sichuan/
│  ├─ attractions.md
│  ├─ food.md
│  ├─ season.md
│  └─ transport.md
└─ yunnan/
   ├─ attractions.md
   ├─ food.md
   ├─ season.md
   └─ transport.md
```

系统会根据用户填写的目的地选择知识库：

```text
四川 / 成都 / 川西 -> common + sichuan
云南 / 昆明 / 大理 / 丽江 -> common + yunnan
未知目的地 -> common
```

扩展新目的地时：

1. 在 `knowledge/` 下新增目录，例如 `beijing/`。
2. 添加 `attractions.md`、`food.md`、`season.md`、`transport.md` 等资料。
3. 在 `DestinationResolver` 中增加目的地别名映射。
4. 补充对应检索测试。

## 后续规划

- 增加天气、路线和景点推荐工具服务。
- 增加 Spring AI Alibaba Tool Calling / ReactAgent 版本接口。
- 将轻量级关键词 RAG 升级为 DashScope Embedding + 向量检索。
- 增加前端 E2E 测试和部署配置。
