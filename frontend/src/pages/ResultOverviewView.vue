<template>
  <AppShell>
    <EmptyTripState
      v-if="!hasResult"
      title="还没有可查看的旅行计划"
      description="先创建一次行程，生成完成后这里会展示概览、预算分析和知识库来源。"
      icon="map"
    />

    <template v-else>
    <section class="trip-hero">
      <div>
        <span class="hero-badge">{{ result.destination }} · {{ result.totalDays }} 天</span>
        <h1 class="hero-title" style="margin-top: 16px">玩转{{ result.destination }}</h1>
        <p style="max-width: 720px; line-height: 1.8">{{ result.summary }}</p>
      </div>
      <RouterLink class="ghost-action" to="/plan" style="position: absolute; right: 34px; bottom: 34px">修改需求</RouterLink>
    </section>

    <section class="content-grid">
      <div>
        <RequestSummaryCard :request="request" />
        <ConstraintInsightCard :request="request" :result="result" />

        <article class="panel">
          <p class="section-eyebrow">行程总结</p>
          <h2 class="section-title" style="font-size: 22px">基于知识库与预算生成</h2>
          <p class="section-copy">{{ result.summary }}</p>
          <div class="overview-stat-grid">
            <div class="overview-stat">
              <span>旅行天数</span>
              <strong>{{ result.totalDays }} 天</strong>
            </div>
            <div class="overview-stat">
              <span>出行人数</span>
              <strong>{{ request.travelers }} 人同行</strong>
            </div>
            <div class="overview-stat">
              <span>总预算</span>
              <strong>¥{{ request.budget }}</strong>
            </div>
          </div>
        </article>

        <article class="panel">
          <p class="section-eyebrow">亮点</p>
          <div class="mini-list">
            <div v-for="day in result.days.slice(0, 3)" :key="day.day" class="insight-card">
              <div class="source-icon">
                <span class="material-symbols-outlined">route</span>
              </div>
              <div>
                <p class="source-line">第 {{ day.day }} 天</p>
                <h3 style="margin: 4px 0 8px">{{ day.theme }}</h3>
                <p class="section-copy">{{ day.transportTip }}</p>
              </div>
            </div>
          </div>
          <RouterLink class="ghost-action" to="/days" style="margin-top: 18px; width: 100%">查看完整行程</RouterLink>
        </article>
      </div>

      <aside class="panel">
        <article class="generation-summary-card">
          <p class="section-eyebrow">生成模式</p>
          <h3>{{ generationModeLabel }}</h3>
          <p class="section-copy">{{ generationModeDescription }}</p>
          <div v-if="generationMetadata" class="generation-meta-row">
            <span>{{ generationMetadata.validated ? '已通过校验' : '未校验' }}</span>
            <span>尝试 {{ generationMetadata.attempts }} 次</span>
            <span>{{ generationMetadata.mode }}</span>
          </div>
        </article>

        <ToolEvidencePanel :mode="tripState.generationMode" :request="request" :result="result" />

        <KnowledgeBaseSummary :references="result.references" />

        <article v-if="result.weatherInfo" class="weather-card">
          <p class="section-eyebrow">天气提醒</p>
          <h3>{{ result.weatherInfo.riskLevel }}</h3>
          <p class="section-copy">{{ result.weatherInfo.summary }}</p>
          <p class="section-copy" style="margin-top: 10px">{{ result.weatherInfo.suggestion }}</p>
        </article>

        <p class="section-eyebrow">预算概览</p>
        <div class="budget-bars">
          <div class="bar-row">
            <span>人均预算</span>
            <strong>¥{{ result.budgetAnalysis?.perPersonBudget ?? 0 }}</strong>
            <div class="bar"><span :style="{ width: perPersonBudgetWidth }"></span></div>
          </div>
          <div class="bar-row">
            <span>每日人均</span>
            <strong>¥{{ result.budgetAnalysis?.perPersonDailyBudget ?? 0 }}</strong>
            <div class="bar"><span :style="{ width: dailyBudgetWidth }"></span></div>
          </div>
          <div class="bar-row">
            <span>舒适度</span>
            <strong>{{ result.budgetAnalysis?.level ?? '适中' }}</strong>
            <div class="bar"><span :style="{ width: comfortLevelWidth }"></span></div>
          </div>
        </div>
        <p class="section-copy" style="margin-top: 22px">{{ result.budgetAnalysis?.suggestion }}</p>
        <RouterLink class="primary-action" to="/days" style="margin-top: 20px; width: 100%">查看行程</RouterLink>
        <RouterLink class="ghost-action" to="/references" style="margin-top: 12px; width: 100%">查看引用来源</RouterLink>
        <button class="ghost-action" type="button" style="margin-top: 12px; width: 100%" @click="copySummary">复制行程摘要</button>
        <button class="ghost-action" type="button" style="margin-top: 12px; width: 100%" @click="exportMarkdown">导出 Markdown</button>
        <button class="ghost-action" type="button" style="margin-top: 12px; width: 100%" @click="restartPlan">清空并重新规划</button>
        <p v-if="copyMessage" class="inline-feedback">{{ copyMessage }}</p>
      </aside>
    </section>
    </template>
  </AppShell>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppShell from '../components/AppShell.vue'
import ConstraintInsightCard from '../components/ConstraintInsightCard.vue'
import EmptyTripState from '../components/EmptyTripState.vue'
import KnowledgeBaseSummary from '../components/KnowledgeBaseSummary.vue'
import RequestSummaryCard from '../components/RequestSummaryCard.vue'
import ToolEvidencePanel from '../components/ToolEvidencePanel.vue'
import { resetTripSession, tripState } from '../store/tripStore'
import { copyTextToClipboard } from '../utils/clipboard'
import { downloadTextFile } from '../utils/download'
import { createTripPlanMarkdownFilename, formatTripPlanMarkdown, formatTripPlanSummary } from '../utils/tripFormatter'

const router = useRouter()
const hasResult = computed(() => tripState.hasResult)
const result = computed(() => tripState.result)
const request = computed(() => tripState.request)
const generationMetadata = computed(() => result.value.generationMetadata)
const generationModeLabel = computed(() => (tripState.generationMode === 'agent' ? 'Agent Tool Calling' : '稳定服务编排'))
const generationModeDescription = computed(() =>
  tripState.generationMode === 'agent'
    ? '大模型通过工具调用预算、天气和知识库能力后生成行程。'
    : '后端按固定顺序编排知识库、预算、天气和大模型生成流程。'
)
const copyMessage = ref('')
const perPersonBudgetWidth = computed(() => getProgressWidth(result.value.budgetAnalysis?.perPersonBudget ?? 0, 6000))
const dailyBudgetWidth = computed(() => getProgressWidth(result.value.budgetAnalysis?.perPersonDailyBudget ?? 0, 1500))
const comfortLevelWidth = computed(() => `${getComfortProgress(result.value.budgetAnalysis?.level ?? '')}%`)

function getProgressWidth(value: number, maxValue: number) {
  if (value <= 0) {
    return '0%'
  }

  const progress = Math.min(Math.max((value / maxValue) * 100, 12), 100)
  return `${Math.round(progress)}%`
}

function getComfortProgress(level: string) {
  if (level.includes('经济')) {
    return 36
  }
  if (level.includes('奢华') || level.includes('宽裕') || level.includes('高')) {
    return 92
  }
  if (level.includes('舒适')) {
    return 76
  }
  return 62
}

async function copySummary() {
  copyMessage.value = ''
  try {
    await copyTextToClipboard(formatTripPlanSummary(result.value, request.value))
    copyMessage.value = '已复制行程摘要'
  } catch {
    copyMessage.value = '复制失败，请稍后重试'
  }
}

function exportMarkdown() {
  downloadTextFile(
    createTripPlanMarkdownFilename(result.value.destination),
    formatTripPlanMarkdown(result.value, request.value),
    'text/markdown;charset=utf-8',
  )
}

async function restartPlan() {
  resetTripSession()
  await router.push('/plan')
}
</script>
