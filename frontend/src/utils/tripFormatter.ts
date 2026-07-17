import type { ItineraryDay, KnowledgeReference, PlanTripRequest, TripPlanResponse } from '../types'

export function createTripPlanMarkdownFilename(destination: string, createdAt = new Date()) {
  const dateText = createdAt.toISOString().slice(0, 10)
  const safeDestination = destination.replace(/[\\/:*?"<>|]/g, '')
  return `${safeDestination || 'travel-plan'}-${dateText}.md`
}

export function formatTripPlanSummary(result: TripPlanResponse, request: PlanTripRequest): string {
  const dayLines = result.days
    .map(
      (day) => `Day ${day.day}｜${day.theme}
上午：${day.morning}
下午：${day.afternoon}
晚上：${day.evening}
交通：${day.transportTip}
天气：${result.weatherInfo?.dailyTips?.[day.day - 1] ?? '暂无当天天气提醒'}`,
    )
    .join('\n\n')

  const referenceLines = result.references
    .map((reference) => `- ${reference.title}（${reference.source}，分数 ${reference.score}）`)
    .join('\n')

  const toolCallLines = result.toolCalls
    ?.map((toolCall) => `- ${toolCall.displayName}｜${toolCall.status}｜${toolCall.detail}`)
    .join('\n')

  return `智能旅行规划
目的地：${result.destination}
出发地：${request.departureCity}
天数：${result.totalDays}
人数：${request.travelers}
预算：${request.budget} 元

行程摘要：
${result.summary}

每日安排：
${dayLines}

知识库引用：
${referenceLines || '暂无引用'}

天气提醒：
${result.weatherInfo?.summary ?? '暂无天气提醒'}
${result.weatherInfo?.suggestion ?? ''}

生成校验：
模式：${result.generationMetadata?.mode ?? '未知'}
尝试次数：${result.generationMetadata?.attempts ?? 1}
校验状态：${result.generationMetadata?.validated ? '已通过' : '未知'}

工具调用记录：
${toolCallLines || '暂无工具调用记录'}`
}

export function formatTripPlanMarkdown(result: TripPlanResponse, request: PlanTripRequest): string {
  const preferenceText = request.preferences.length > 0 ? request.preferences.join('、') : '未填写'
  const avoidText = request.avoid.length > 0 ? request.avoid.join('、') : '未填写'
  const daySections = result.days
    .map(
      (day) => `## Day ${day.day}｜${day.theme}

- 上午：${day.morning}
- 下午：${day.afternoon}
- 晚上：${day.evening}
- 交通建议：${day.transportTip}
- 天气提醒：${result.weatherInfo?.dailyTips?.[day.day - 1] ?? '暂无当天天气提醒'}`,
    )
    .join('\n\n')

  const referenceLines = result.references
    .map((reference) => `- **${reference.title}**：${reference.source}，匹配分数 ${reference.score}\n  ${reference.snippet}`)
    .join('\n')

  const toolCallLines = result.toolCalls
    ?.map((toolCall) => `- **${toolCall.displayName}**：${toolCall.status}，${toolCall.detail}`)
    .join('\n')

  return `# ${result.destination}${result.totalDays}天智能旅行计划

## 用户需求

- 出发地：${request.departureCity}
- 目的地：${result.destination}
- 天数：${result.totalDays}
- 人数：${request.travelers}
- 预算：${request.budget} 元
- 偏好：${preferenceText}
- 避坑项：${avoidText}

## 行程摘要

${result.summary}

## 预算分析

- 人均预算：${result.budgetAnalysis?.perPersonBudget ?? 0} 元
- 每日人均：${result.budgetAnalysis?.perPersonDailyBudget ?? 0} 元
- 预算等级：${result.budgetAnalysis?.level ?? '暂无'}
- 建议：${result.budgetAnalysis?.suggestion ?? '暂无预算建议'}

## 天气提醒

- 风险等级：${result.weatherInfo?.riskLevel ?? '暂无'}
- 摘要：${result.weatherInfo?.summary ?? '暂无天气摘要'}
- 建议：${result.weatherInfo?.suggestion ?? '暂无天气建议'}

## 生成校验

- 生成模式：${result.generationMetadata?.mode ?? '未知'}
- 尝试次数：${result.generationMetadata?.attempts ?? 1}
- 校验状态：${result.generationMetadata?.validated ? '已通过' : '未知'}

${daySections}

## 知识库引用

${referenceLines || '暂无引用'}

## 工具调用记录

${toolCallLines || '暂无工具调用记录'}
`
}

export function formatKnowledgeReference(reference: KnowledgeReference): string {
  return `知识库引用
标题：${reference.title}
来源：${reference.source}
匹配分数：${reference.score}

内容片段：
${reference.snippet}`
}

export function formatItineraryDay(destination: string, day: ItineraryDay, weatherTip = ''): string {
  return `${destination}行程 Day ${day.day}
主题：${day.theme}

上午：
${day.morning}

下午：
${day.afternoon}

晚上：
${day.evening}

交通建议：
${day.transportTip}

天气提醒：
${weatherTip || '暂无当天天气提醒'}`
}
