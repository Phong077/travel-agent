<template>
  <article class="constraint-insight-card">
    <p class="section-eyebrow">需求处理说明</p>
    <h3>系统如何理解本次需求</h3>

    <div class="constraint-insight-list">
      <div v-for="item in insightItems" :key="item.title" class="constraint-insight-item">
        <span class="material-symbols-outlined">{{ item.icon }}</span>
        <div>
          <strong>{{ item.title }}</strong>
          <p>{{ item.description }}</p>
        </div>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { PlanTripRequest, TripPlanResponse } from '../types'

const props = defineProps<{
  request: PlanTripRequest
  result: TripPlanResponse
}>()

const insightItems = computed(() => {
  const items = [
    {
      icon: 'favorite',
      title: '偏好匹配',
      description:
        props.request.preferences.length > 0
          ? `已优先围绕“${props.request.preferences.join('、')}”组织景点、餐饮和体验内容。`
          : '本次没有填写明确偏好，系统会按通用旅行节奏生成行程。',
    },
    {
      icon: 'payments',
      title: '预算控制',
      description: props.result.budgetAnalysis?.suggestion ?? '系统会根据总预算、人数和天数控制住宿、交通与餐饮强度。',
    },
  ]

  if (hasAvoidKeyword(['早起', '太早'])) {
    items.push({
      icon: 'schedule',
      title: '避免早起',
      description: '行程会尽量避免过早出发，优先安排更轻松的上午节奏。',
    })
  }

  if (hasAvoidKeyword(['换酒店', '频繁'])) {
    items.push({
      icon: 'hotel',
      title: '减少换住',
      description: '系统会优先选择交通便利城市或区域作为基地，减少每天搬运行李的成本。',
    })
  }

  if (props.result.weatherInfo) {
    items.push({
      icon: 'wb_cloudy',
      title: '天气风险',
      description: `${props.result.weatherInfo.riskLevel}：${props.result.weatherInfo.suggestion}`,
    })
  }

  return items
})

function hasAvoidKeyword(keywords: string[]) {
  return props.request.avoid.some((item) => keywords.some((keyword) => item.includes(keyword)))
}
</script>
