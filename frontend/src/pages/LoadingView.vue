<template>
  <div class="loading-wrap">
    <section class="loading-board">
      <div class="skeleton-line"></div>
      <div class="skeleton-grid">
        <div class="skeleton-card large"></div>
        <div class="skeleton-card"></div>
        <div class="skeleton-card small"></div>
        <div class="skeleton-card small"></div>
        <div class="skeleton-card small"></div>
      </div>

      <div class="loading-orb">
        <div class="spinner-ring"></div>
        <span class="material-symbols-outlined">smart_toy</span>
      </div>

      <div class="loading-copy">
        <p class="section-eyebrow">{{ activeStep.label }}</p>
        <h1 class="section-title">正在{{ activeStep.title }}...</h1>
        <p class="section-copy">{{ activeStep.description }}</p>
        <span class="loading-mode-pill">当前模式：{{ generationModeLabel }}</span>
      </div>

      <div class="loading-steps">
        <div v-for="step in steps" :key="step.label" class="loading-step" :class="{ active: step.label === activeStep.label }">
          <span class="material-symbols-outlined">{{ step.icon }}</span>
          <strong>{{ step.label }}</strong>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { tripState } from '../store/tripStore'
import { getGenerationModeLabel } from '../utils/generationMode'

const destinationName = computed(() => tripState.request.destination || '目的地')
const generationModeLabel = computed(() => getGenerationModeLabel(tripState.generationMode))

const steps = computed(() => [
  {
    label: '分析预算',
    title: '分析预算',
    description: '计算人均预算、每日预算和当前出行舒适度。',
    icon: 'payments',
  },
  {
    label: '检索知识库',
    title: `检索${destinationName.value}知识库`,
    description: '匹配景点、美食、交通、季节等资料，形成可引用上下文。',
    icon: 'travel_explore',
  },
  {
    label: '生成行程',
    title: '生成结构化行程',
    description: '把检索结果、偏好和避开事项整合为每日安排。',
    icon: 'auto_awesome',
  },
])

const activeIndex = ref(0)
let timer: number | undefined

const activeStep = computed(() => steps.value[activeIndex.value])

onMounted(() => {
  timer = window.setInterval(() => {
    activeIndex.value = (activeIndex.value + 1) % steps.value.length
  }, 900)
})

onUnmounted(() => {
  if (timer) {
    window.clearInterval(timer)
  }
})
</script>
