# 智能旅行助手前端

基于 Vue 3 + Vite + TypeScript 实现的智能旅行规划助手前端页面，按照 Stitch 高保真原型进行复刻和组件化开发。

## 技术栈

- Vue 3
- TypeScript
- Vite
- Vue Router
- 原生 CSS 组件样式
- Material Symbols 图标

## 页面路由

| 路径 | 页面 |
|---|---|
| `/` | 控制台首页 |
| `/plan` | 旅行规划输入表单 |
| `/loading` | 生成中加载状态 |
| `/result` | 旅行计划概览与预算分析 |
| `/days` | 每日行程详情 |
| `/references` | RAG 引用来源 |
| `/knowledge` | 知识库检索调试页 |

## 后端接口

开发环境通过 Vite 代理转发到 Spring Boot：

```text
前端: http://localhost:5173
后端: http://localhost:8092
```

已接入接口：

```text
POST /api/trips/plan
POST /api/knowledge/search
```

如果后端未启动，前端会自动使用 mock 数据兜底，并在页面顶部展示提示，方便单独演示前端效果。

## 本地运行

```bash
npm install
npm run dev
```

访问：

```text
http://localhost:5173
```

## 构建

```bash
npm run build
```

## 项目亮点

- 按 Stitch 原型拆分页面和组件。
- 支持桌面端侧边栏布局与移动端底部导航。
- 行程生成流程包含加载态、结果概览、每日详情、预算分析和 RAG 引用展示。
- 知识库调试页可单独验证 RAG 检索结果。
- 前端类型与后端 DTO 对齐，方便联调和面试讲解。
