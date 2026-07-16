# 智能旅行规划助手

基于 Spring Boot、Spring AI Alibaba 和阿里百炼构建的智能旅行规划助手。项目支持根据用户的出发地、目的地、天数、人数、预算、偏好和避免项生成结构化旅行计划，并结合本地四川旅行知识库进行轻量级 RAG 检索增强。

## 核心功能

- 结构化旅行计划生成：按天返回主题、上午、下午、晚上和交通建议。
- 轻量级 RAG：读取本地 Markdown 知识库，基于加权关键词召回 TopK 参考资料。
- 引用来源返回：接口返回 references，展示本次生成参考了哪些知识片段。
- 预算分析：根据总预算、人数和天数计算人均预算、每日预算和预算等级。
- 避免项约束：支持避免太早起床、频繁换酒店等用户限制。
- 统一响应与异常处理：成功和失败响应格式统一，便于前端处理。
- 调试日志：输出检索关键词、命中知识、预算分析和 AI 原始返回。

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
  -> 提取目的地、偏好等关键词
  -> 加权关键词检索本地 Markdown 知识库
  -> 返回 TopK references
  -> 将 references 注入 Prompt
  -> 调用阿里百炼生成结构化 JSON
  -> Jackson 解析为 TripPlanResponse
  -> 返回旅行计划、预算分析和引用来源
```

## 后续规划

- 增加天气、路线和景点推荐工具服务。
- 增加 Spring AI Alibaba Tool Calling / ReactAgent 版本接口。
- 将轻量级关键词 RAG 升级为 DashScope Embedding + 向量检索。
- 增加前端 E2E 测试和部署配置。
