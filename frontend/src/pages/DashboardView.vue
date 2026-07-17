<template>
  <AppShell>
    <section>
      <p class="section-eyebrow">智能旅行助手</p>
      <h1 class="hero-title">你好，Kristin！</h1>
      <p class="section-copy">准备好规划下一次冒险了吗？让 AI 为你安排有依据、有预算、有节奏的个性化行程。</p>
    </section>

    <section class="dashboard-grid" style="margin-top: 32px">
      <div>
        <article class="analytics-card travel-showcase-card">
          <div class="showcase-header">
            <div>
              <p class="section-eyebrow">{{ hasResult ? '最近计划' : '当前推荐' }}</p>
              <h2>{{ showcaseTitle }}</h2>
            </div>
            <span class="score-pill">RAG {{ referenceCount }} 条引用</span>
          </div>
          <div class="fake-dashboard">
            <div class="metric">
              <span>偏好匹配</span>
              <span class="metric-value">{{ preferenceMatch }}%</span>
              <small>{{ preferenceLabel }}</small>
            </div>
            <div class="metric blue">
              <span>知识库命中</span>
              <span class="metric-value">{{ knowledgeHitRate }}%</span>
              <small>{{ knowledgeLabel }}</small>
            </div>
            <div class="mini-list" style="grid-column: 1 / -1">
              <div v-for="item in showcaseRows" :key="item.label" class="mini-row">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </div>
            </div>
          </div>
          <div class="line-chart"></div>
          <RouterLink class="primary-action showcase-action" :to="hasResult ? '/result' : '/plan'">
            <span class="material-symbols-outlined">auto_awesome</span>
            <span>{{ hasResult ? '查看计划' : '开始计划' }}</span>
          </RouterLink>
        </article>

        <div style="margin-top: 22px">
          <p class="section-eyebrow">{{ hasResult ? '行程概览' : '推荐路线' }}</p>
          <h2 class="section-title">{{ summaryTitle }}</h2>
          <p class="section-copy">{{ summaryCopy }}</p>
          <RouterLink class="primary-action" to="/plan" style="margin-top: 18px">
            <span class="material-symbols-outlined">auto_awesome</span>
            <span>{{ hasResult ? '重新规划' : '开始计划' }}</span>
          </RouterLink>
        </div>
      </div>

      <aside>
        <section class="panel trip-preview-panel">
          <p class="section-eyebrow">{{ hasResult ? '当前行程' : '即将到来' }}</p>
          <h3>{{ previewTitle }}</h3>
          <p class="section-copy">{{ previewMeta }}</p>
          <div class="trip-preview-image">
            <span>{{ request.departureCity }} 出发</span>
            <strong>{{ hasResult ? result.destination : '等待填写' }}</strong>
          </div>
          <div class="bar-row" style="margin-top: 18px">
            <span>准备进度</span>
            <strong>{{ readiness }}%</strong>
            <div class="bar"><span :style="{ width: `${readiness}%` }"></span></div>
          </div>
        </section>

        <section class="panel history-insight-panel">
          <div class="history-insight-header">
            <div>
              <p class="section-eyebrow">历史洞察</p>
              <h3>{{ historyInsightTitle }}</h3>
            </div>
            <RouterLink class="ghost-action" to="/history">查看</RouterLink>
          </div>
          <div class="history-insight-grid">
            <div>
              <span>总计划</span>
              <strong>{{ historyCount }}</strong>
            </div>
            <div>
              <span>Agent 版</span>
              <strong>{{ agentHistoryCount }}</strong>
            </div>
            <div>
              <span>稳定版</span>
              <strong>{{ stableHistoryCount }}</strong>
            </div>
          </div>
          <p class="section-copy">{{ historyInsightCopy }}</p>
        </section>

        <section class="panel">
          <p class="section-eyebrow">最近事项</p>
          <div class="mini-list">
            <RouterLink class="mini-row" to="/knowledge">
              <span>知识库检索调试</span>
              <span class="material-symbols-outlined">arrow_forward</span>
            </RouterLink>
            <RouterLink class="mini-row" to="/references">
              <span>引用来源查看</span>
              <span class="material-symbols-outlined">arrow_forward</span>
            </RouterLink>
            <RouterLink class="mini-row" to="/days">
              <span>每日行程详情</span>
              <span class="material-symbols-outlined">arrow_forward</span>
            </RouterLink>
            <RouterLink class="mini-row" to="/history">
              <span>生成历史记录</span>
              <span class="material-symbols-outlined">arrow_forward</span>
            </RouterLink>
          </div>
        </section>
      </aside>
    </section>
  </AppShell>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import AppShell from '../components/AppShell.vue'
import { tripState } from '../store/tripStore'

const hasResult = computed(() => tripState.hasResult)
const result = computed(() => tripState.result)
const request = computed(() => tripState.request)
const historyCount = computed(() => tripState.history.length)
const agentHistoryCount = computed(() => tripState.history.filter((item) => item.generationMode === 'agent').length)
const stableHistoryCount = computed(() => tripState.history.filter((item) => item.generationMode === 'stable').length)
const latestHistoryItem = computed(() => tripState.history[0])
const referenceCount = computed(() => (hasResult.value ? result.value.references.length : 0))
const preferenceMatch = computed(() => Math.min(96, 60 + request.value.preferences.length * 8))
const knowledgeHitRate = computed(() => (hasResult.value ? Math.min(96, 38 + referenceCount.value * 12) : 0))
const readiness = computed(() => (hasResult.value ? 100 : 20))
const showcaseTitle = computed(() => (hasResult.value ? `探索${result.value.destination}` : '探索你的下一站'))
const preferenceLabel = computed(() => (request.value.preferences.length > 0 ? request.value.preferences.slice(0, 2).join('与') : '等待选择偏好'))
const knowledgeLabel = computed(() => (hasResult.value ? '目的地资料与通用规则' : '生成后显示命中情况'))
const summaryTitle = computed(() => (hasResult.value ? `${result.value.destination} · ${result.value.totalDays} 天计划` : '按目的地动态检索资料'))
const summaryCopy = computed(() =>
  hasResult.value ? result.value.summary : '发现当地美食、交通方式和自然风景，生成更适合预算和偏好的旅行计划。',
)
const previewTitle = computed(() => (hasResult.value ? `${result.value.destination}旅行计划` : '下一次旅行计划'))
const previewMeta = computed(() => `${request.value.days} 天 · ${request.value.travelers} 人 · 预算 ${request.value.budget}`)
const historyInsightTitle = computed(() => (historyCount.value > 0 ? '已沉淀旅行方案' : '暂无历史记录'))
const historyInsightCopy = computed(() =>
  latestHistoryItem.value
    ? `最近一次生成：${latestHistoryItem.value.result.destination} · ${latestHistoryItem.value.result.totalDays} 天，使用${latestHistoryItem.value.generationMode === 'agent' ? 'Agent Tool Calling' : '稳定服务编排'}。`
    : '生成行程后，系统会自动保存本地历史，方便恢复、筛选和导出。',
)
const showcaseRows = computed(() => {
  if (!hasResult.value) {
    return [
      { label: '填写目的地', value: '第 1 步' },
      { label: '选择旅行偏好', value: '第 2 步' },
      { label: '生成结构化行程', value: '第 3 步' },
    ]
  }

  return result.value.days.slice(0, 3).map((day) => ({
    label: day.theme,
    value: `第 ${day.day} 天`,
  }))
})
</script>
