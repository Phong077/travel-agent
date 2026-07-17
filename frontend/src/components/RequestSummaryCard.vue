<template>
  <article class="request-summary-card">
    <div class="request-summary-header">
      <div>
        <p class="section-eyebrow">用户需求</p>
        <h3>{{ request.departureCity }} → {{ request.destination }}</h3>
      </div>
      <span class="request-summary-badge">{{ request.days }} 天</span>
    </div>

    <div class="request-summary-stats">
      <div>
        <span>出行人数</span>
        <strong>{{ request.travelers }} 人</strong>
      </div>
      <div>
        <span>总预算</span>
        <strong>¥{{ request.budget }}</strong>
      </div>
      <div>
        <span>人均预算</span>
        <strong>¥{{ perPersonBudget }}</strong>
      </div>
    </div>

    <div class="request-chip-group">
      <p>偏好</p>
      <div>
        <span v-for="item in preferenceItems" :key="item" class="request-chip">{{ item }}</span>
      </div>
    </div>

    <div class="request-chip-group avoid">
      <p>避坑项</p>
      <div>
        <span v-for="item in avoidItems" :key="item" class="request-chip">{{ item }}</span>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { PlanTripRequest } from '../types'

const props = defineProps<{
  request: PlanTripRequest
}>()

const perPersonBudget = computed(() => Math.round(props.request.budget / Math.max(props.request.travelers, 1)))
const preferenceItems = computed(() => (props.request.preferences.length > 0 ? props.request.preferences : ['未填写偏好']))
const avoidItems = computed(() => (props.request.avoid.length > 0 ? props.request.avoid : ['未填写避坑项']))
</script>
