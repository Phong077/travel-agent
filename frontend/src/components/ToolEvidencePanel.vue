<template>
  <article class="tool-evidence-panel">
    <div class="tool-evidence-header">
      <div>
        <p class="section-eyebrow">工具证据</p>
        <h3>{{ title }}</h3>
      </div>
      <span class="tool-evidence-badge">{{ modeLabel }}</span>
    </div>

    <div class="tool-evidence-list">
      <div v-for="item in evidenceItems" :key="item.name" class="tool-evidence-item">
        <div class="source-icon">
          <span class="material-symbols-outlined">{{ item.icon }}</span>
        </div>
        <div>
          <div class="tool-evidence-title">
            <strong>{{ item.name }}</strong>
            <span :class="['tool-evidence-status', item.available ? 'ready' : 'muted']">
              {{ item.status }}
            </span>
          </div>
          <p class="section-copy">{{ item.description }}</p>
          <p class="tool-evidence-value">{{ item.value }}</p>
        </div>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { GenerationMode } from '../store/tripStore'
import type { PlanTripRequest, TripPlanResponse } from '../types'

const props = defineProps<{
  mode: GenerationMode
  request: PlanTripRequest
  result: TripPlanResponse
}>()

const title = computed(() => (props.mode === 'agent' ? 'Agent 调用链路' : '服务编排链路'))
const modeLabel = computed(() => (props.mode === 'agent' ? 'Tool Calling' : 'Stable Flow'))

const evidenceItems = computed(() => {
  const toolCalls = props.result.toolCalls ?? []
  if (toolCalls.length > 0) {
    return toolCalls.map((item) => ({
      name: item.displayName,
      icon: getToolIcon(item.name),
      available: true,
      status: item.status,
      description: getToolDescription(item.name),
      value: item.detail,
    }))
  }

  const budget = props.result.budgetAnalysis
  const weather = props.result.weatherInfo
  const references = props.result.references ?? []
  const bestReferenceScore = references.length > 0 ? Math.max(...references.map((item) => item.score)) : 0

  return [
    {
      name: '预算分析工具',
      icon: 'payments',
      available: Boolean(budget),
      status: budget ? '已使用' : '未返回',
      description: '根据总预算、人数和天数计算人均预算与舒适度。',
      value: budget
        ? `人均 ¥${budget.perPersonBudget}，每日人均 ¥${budget.perPersonDailyBudget}，${budget.level}`
        : `请求预算 ¥${props.request.budget}，等待后端返回分析结果`,
    },
    {
      name: '天气风险工具',
      icon: 'wb_cloudy',
      available: Boolean(weather),
      status: weather ? '已使用' : '未返回',
      description: '根据目的地匹配天气风险与出行建议。',
      value: weather ? `${weather.riskLevel}：${weather.suggestion}` : `${props.request.destination} 暂无天气提示`,
    },
    {
      name: '知识库检索工具',
      icon: 'travel_explore',
      available: references.length > 0,
      status: references.length > 0 ? '已使用' : '未返回',
      description: '从目的地知识库中召回景点、交通、美食和季节资料。',
      value:
        references.length > 0
          ? `命中 ${references.length} 条引用，最高相关度 ${bestReferenceScore}`
          : '没有返回可展示的知识库引用',
    },
  ]
})

function getToolIcon(name: string) {
  if (name.includes('Budget') || name.includes('budget')) {
    return 'payments'
  }
  if (name.includes('Weather') || name.includes('weather')) {
    return 'wb_cloudy'
  }
  if (name.includes('Knowledge') || name.includes('knowledge')) {
    return 'travel_explore'
  }
  return 'construction'
}

function getToolDescription(name: string) {
  if (name.includes('Budget') || name.includes('budget')) {
    return '根据总预算、人数和天数计算人均预算与舒适度。'
  }
  if (name.includes('Weather') || name.includes('weather')) {
    return '根据目的地匹配天气风险与出行建议。'
  }
  if (name.includes('Knowledge') || name.includes('knowledge')) {
    return '从目的地知识库中召回景点、交通、美食和季节资料。'
  }
  return '后端返回的辅助能力调用记录。'
}
</script>
