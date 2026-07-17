import type { ItineraryDay, KnowledgeReference, PlanTripRequest, TripPlanResponse } from '../types'

export function formatTripPlanSummary(result: TripPlanResponse, request: PlanTripRequest): string {
  const dayLines = result.days
    .map(
      (day) => `Day ${day.day}｜${day.theme}
上午：${day.morning}
下午：${day.afternoon}
晚上：${day.evening}
交通：${day.transportTip}`,
    )
    .join('\n\n')

  const referenceLines = result.references
    .map((reference) => `- ${reference.title}（${reference.source}，分数 ${reference.score}）`)
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
${referenceLines || '暂无引用'}`
}

export function formatKnowledgeReference(reference: KnowledgeReference): string {
  return `知识库引用
标题：${reference.title}
来源：${reference.source}
匹配分数：${reference.score}

内容片段：
${reference.snippet}`
}

export function formatItineraryDay(destination: string, day: ItineraryDay): string {
  return `${destination}行程 Day ${day.day}
主题：${day.theme}

上午：
${day.morning}

下午：
${day.afternoon}

晚上：
${day.evening}

交通建议：
${day.transportTip}`
}
