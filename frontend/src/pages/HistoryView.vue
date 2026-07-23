<template>
  <AppShell>
    <section>
      <p class="section-eyebrow">生成历史</p>
      <h1 class="section-title">最近的旅行计划</h1>
      <p class="section-copy">这里保存在当前浏览器生成过的行程快照，可以快速恢复到某一次规划结果。</p>
    </section>

    <EmptyTripState
      v-if="historyItems.length === 0"
      title="还没有历史记录"
      description="生成一次旅行计划后，系统会自动把结果保存到本地历史中。"
      icon="history"
      style="margin-top: 28px"
    />

    <section v-else class="history-layout" style="margin-top: 28px">
      <div class="history-toolbar">
        <p class="section-eyebrow">共 {{ historyItems.length }} 条记录，当前显示 {{ filteredHistoryItems.length }} 条</p>
        <button class="ghost-action" type="button" @click="clearHistory">清空历史</button>
      </div>

      <div class="history-filter-panel">
        <div class="field-control">
          <span class="material-symbols-outlined">search</span>
          <input v-model="searchKeyword" placeholder="搜索目的地、出发地、偏好或摘要" />
        </div>
        <select v-model="selectedMode" class="history-mode-select">
          <option value="all">全部模式</option>
          <option value="stable">稳定服务编排</option>
          <option value="agent">Agent Tool Calling</option>
          <option value="react-agent">ReactAgent</option>
          <option value="multi-agent">多 Agent 协同</option>
        </select>
      </div>

      <EmptyTripState
        v-if="filteredHistoryItems.length === 0"
        title="没有匹配的历史记录"
        description="可以换一个关键词，或者切换生成模式筛选条件。"
        icon="search_off"
      />

      <div class="history-grid">
        <article v-for="item in filteredHistoryItems" :key="item.id" class="history-card">
          <div class="history-card-header">
            <div>
              <span class="history-mode-pill">{{ getModeLabel(item.generationMode) }}</span>
              <h2>{{ item.result.destination }} · {{ item.result.totalDays }} 天</h2>
            </div>
            <span class="history-date">{{ formatDate(item.createdAt) }}</span>
          </div>

          <p class="section-copy">{{ item.result.summary }}</p>

          <div class="history-meta-grid">
            <div>
              <span>出发</span>
              <strong>{{ item.request.departureCity }}</strong>
            </div>
            <div>
              <span>人数</span>
              <strong>{{ item.request.travelers }} 人</strong>
            </div>
            <div>
              <span>预算</span>
              <strong>¥{{ item.request.budget }}</strong>
            </div>
            <div>
              <span>引用</span>
              <strong>{{ item.result.references.length }} 条</strong>
            </div>
            <div>
              <span>尝试</span>
              <strong>{{ item.result.generationMetadata?.attempts ?? 1 }} 次</strong>
            </div>
            <div>
              <span>校验</span>
              <strong>{{ item.result.generationMetadata?.validated ? '通过' : '未知' }}</strong>
            </div>
          </div>

          <div class="history-chip-row">
            <span v-for="preference in item.request.preferences.slice(0, 4)" :key="preference" class="request-chip">
              {{ preference }}
            </span>
          </div>

          <div class="history-actions">
            <button class="primary-action" type="button" @click="restoreHistory(item.id)">恢复查看</button>
            <button class="ghost-action" type="button" @click="exportHistory(item.id)">导出</button>
            <button class="ghost-action" type="button" @click="deleteHistory(item.id)">删除</button>
          </div>
        </article>
      </div>
    </section>
  </AppShell>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppShell from '../components/AppShell.vue'
import EmptyTripState from '../components/EmptyTripState.vue'
import { clearTripHistory, deleteTripHistoryItem, loadTripHistoryItem, tripState, type GenerationMode } from '../store/tripStore'
import { downloadTextFile } from '../utils/download'
import { getGenerationModeLabel } from '../utils/generationMode'
import { createTripPlanMarkdownFilename, formatTripPlanMarkdown } from '../utils/tripFormatter'

const router = useRouter()
const searchKeyword = ref('')
const selectedMode = ref<GenerationMode | 'all'>('all')
const historyItems = computed(() => tripState.history)
const filteredHistoryItems = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()

  return historyItems.value.filter((item) => {
    const matchesMode = selectedMode.value === 'all' || item.generationMode === selectedMode.value
    const searchableText = [
      item.request.departureCity,
      item.request.destination,
      item.result.destination,
      item.result.summary,
      item.request.preferences.join(' '),
      item.request.avoid.join(' '),
    ]
      .join(' ')
      .toLowerCase()

    return matchesMode && (!keyword || searchableText.includes(keyword))
  })
})

function getModeLabel(mode: GenerationMode) {
  return getGenerationModeLabel(mode)
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}

async function restoreHistory(id: string) {
  if (loadTripHistoryItem(id)) {
    await router.push('/result')
  }
}

function deleteHistory(id: string) {
  deleteTripHistoryItem(id)
}

function exportHistory(id: string) {
  const item = historyItems.value.find((historyItem) => historyItem.id === id)
  if (!item) {
    return
  }

  downloadTextFile(
    createTripPlanMarkdownFilename(item.result.destination, new Date(item.createdAt)),
    formatTripPlanMarkdown(item.result, item.request),
    'text/markdown;charset=utf-8',
  )
}

function clearHistory() {
  clearTripHistory()
}
</script>
