<template>
  <AppShell>
    <section>
      <p class="section-eyebrow">知识库检索调试</p>
      <h1 class="section-title">验证 RAG 是否命中正确资料</h1>
      <p class="section-copy">
        调试页只调用知识库检索接口，不调用大模型。它可以帮助你向面试官解释“先检索，再生成”的完整链路。
      </p>
    </section>

    <section class="debug-layout" style="margin-top: 28px">
      <div>
        <form class="form-card" @submit.prevent="runSearch">
          <h2 class="section-title" style="font-size: 20px">检索请求参数</h2>
          <p class="section-copy">这些字段会作为检索条件发送给后端，后端再根据目的地选择对应知识库。</p>

          <div class="field-grid" style="margin-top: 22px">
            <label class="field">
              <span>出发地</span>
              <div class="field-control">
                <span class="material-symbols-outlined">flight_takeoff</span>
                <input v-model="form.departureCity" placeholder="重庆" />
              </div>
            </label>
            <label class="field">
              <span>目的地</span>
              <div class="field-control">
                <span class="material-symbols-outlined">location_on</span>
                <input v-model="form.destination" placeholder="四川 / 云南 / 深圳 / 新疆" />
              </div>
            </label>
            <label class="field">
              <span>天数</span>
              <div class="field-control">
                <span class="material-symbols-outlined">date_range</span>
                <input v-model.number="form.days" type="number" min="1" max="15" />
              </div>
            </label>
            <label class="field">
              <span>人数</span>
              <div class="field-control">
                <span class="material-symbols-outlined">group</span>
                <input v-model.number="form.travelers" type="number" min="1" max="12" />
              </div>
            </label>
            <label class="field">
              <span>预算</span>
              <div class="field-control">
                <span class="material-symbols-outlined">payments</span>
                <input v-model.number="form.budget" type="number" min="500" step="100" />
              </div>
            </label>
          </div>

          <div class="debug-form-section">
            <div class="debug-section-header">
              <div>
                <p class="section-eyebrow">偏好关键词</p>
                <p class="section-copy">会提高相关资料的匹配分数。</p>
              </div>
              <div class="debug-add-control">
                <input v-model="preferenceInput" placeholder="例如：古城漫步" @keydown.enter.prevent="addPreference" />
                <button class="ghost-action" type="button" @click="addPreference">添加</button>
              </div>
            </div>
            <div class="preference-grid">
              <button
                v-for="item in form.preferences"
                :key="item"
                type="button"
                class="chip selected"
                @click="removePreference(item)"
              >
                {{ item }}
              </button>
            </div>
          </div>

          <div class="debug-form-section">
            <div class="debug-section-header">
              <div>
                <p class="section-eyebrow">避坑关键词</p>
                <p class="section-copy">会作为约束信息参与检索和后续生成。</p>
              </div>
              <div class="debug-add-control">
                <input v-model="avoidInput" placeholder="例如：排队太久" @keydown.enter.prevent="addAvoid" />
                <button class="ghost-action" type="button" @click="addAvoid">添加</button>
              </div>
            </div>
            <div class="preference-grid">
              <button v-for="item in form.avoid" :key="item" type="button" class="chip danger" @click="removeAvoid(item)">
                {{ item }}
              </button>
            </div>
          </div>

          <div v-if="validationError" class="error-banner">{{ validationError }}</div>

          <div class="page-actions">
            <button class="ghost-action" type="button" @click="fillDestination('四川')">四川样例</button>
            <button class="ghost-action" type="button" @click="fillDestination('云南')">云南样例</button>
            <button class="ghost-action" type="button" @click="fillGuangdongExample">深圳样例</button>
            <button class="ghost-action" type="button" @click="fillDestination('新疆')">通用兜底样例</button>
            <button class="primary-action" type="submit" :disabled="loading">
              <span class="material-symbols-outlined">search</span>
              <span>{{ loading ? '检索中' : '运行检索' }}</span>
            </button>
          </div>
        </form>

        <section class="form-card" style="margin-top: 24px">
          <div class="debug-section-header">
            <div>
              <p class="section-eyebrow">检索结果</p>
              <h2 class="section-title" style="font-size: 20px">命中的知识片段</h2>
            </div>
            <span class="score-pill">命中 {{ results.length }} 条</span>
          </div>

          <div v-if="apiStatus.usingMock" class="error-banner">{{ apiStatus.message }}</div>

          <div v-if="loading" class="debug-state-card">
            <span class="material-symbols-outlined">progress_activity</span>
            <div>
              <h3>正在检索知识库</h3>
              <p class="section-copy">前端正在请求 /api/knowledge/debug，等待后端返回检索链路和匹配文档。</p>
            </div>
          </div>

          <div v-else-if="!hasSearched" class="debug-state-card">
            <span class="material-symbols-outlined">manage_search</span>
            <div>
              <h3>还没有运行检索</h3>
              <p class="section-copy">填写目的地和偏好后点击“运行检索”，这里会展示 RAG 第一步的命中文档。</p>
            </div>
          </div>

          <div v-else-if="results.length === 0" class="debug-state-card">
            <span class="material-symbols-outlined">search_off</span>
            <div>
              <h3>没有命中资料</h3>
              <p class="section-copy">可以换一个目的地，或者补充更明确的偏好关键词。</p>
            </div>
          </div>

          <div v-else class="result-strip" style="margin-top: 20px">
            <article v-for="item in results" :key="item.title + item.source" class="result-strip-card">
              <div class="source-icon">
                <span class="material-symbols-outlined">description</span>
              </div>
              <div>
                <p class="source-line">{{ item.source }}</p>
                <h2 class="section-title" style="font-size: 18px; margin-bottom: 8px">{{ item.title }}</h2>
                <p class="section-copy">{{ item.content }}</p>
              </div>
              <span class="score-pill">分数：{{ item.score }}</span>
            </article>
          </div>

          <div v-if="hasSearched" class="debug-json-panel">
            <div class="debug-json-header">
              <p class="section-eyebrow">响应体预览</p>
              <button class="ghost-action" type="button" @click="copyResponseJson">复制响应</button>
            </div>
            <pre>{{ responseJson }}</pre>
            <p v-if="responseCopyMessage" class="inline-feedback">{{ responseCopyMessage }}</p>
          </div>
        </section>
      </div>

      <aside class="panel">
        <p class="section-eyebrow">调试元数据</p>
        <h2 class="section-title" style="font-size: 20px">本次 RAG 检索路径</h2>

        <div class="budget-bars" style="margin-top: 20px">
          <div class="bar-row">
            <span>目的地 Key</span>
            <strong>{{ debugDestinationKey }}</strong>
          </div>
          <div class="bar-row">
            <span>检索模式</span>
            <strong>{{ debugRetrievalMode }}</strong>
          </div>
          <div class="bar-row">
            <span>专属知识库</span>
            <strong>{{ debugDedicatedLabel }}</strong>
          </div>
          <div class="bar-row">
            <span>向量库</span>
            <strong>{{ debugVectorStoreLabel }}</strong>
          </div>
        </div>

        <div class="debug-json-panel">
          <div class="debug-json-header">
            <p class="section-eyebrow">后端检索 Query</p>
          </div>
          <pre>{{ debugQuery }}</pre>
        </div>

        <KnowledgeBaseSummary :references="knowledgeReferences" />

        <p class="section-eyebrow">查询分析</p>
        <h2 class="section-title" style="font-size: 20px">当前检索链路</h2>
        <p class="section-copy">{{ queryPreview }}</p>

        <div class="budget-bars" style="margin-top: 24px">
          <div class="bar-row">
            <span>检索命中</span>
            <strong>{{ results.length }}</strong>
            <div class="bar"><span :style="{ width: `${Math.min(results.length * 20, 100)}%` }"></span></div>
          </div>
          <div class="bar-row">
            <span>平均分</span>
            <strong>{{ averageScore }}</strong>
            <div class="bar"><span :style="{ width: `${Math.min(Number(averageScore) * 24, 100)}%` }"></span></div>
          </div>
          <div class="bar-row">
            <span>最高分</span>
            <strong>{{ topScore }}</strong>
            <div class="bar"><span :style="{ width: `${Math.min(topScore * 24, 100)}%` }"></span></div>
          </div>
        </div>

        <div class="debug-json-panel">
          <div class="debug-json-header">
            <p class="section-eyebrow">请求体预览</p>
            <button class="ghost-action" type="button" @click="copyRequestJson">复制 JSON</button>
          </div>
          <pre>{{ requestJson }}</pre>
          <p v-if="copyMessage" class="inline-feedback">{{ copyMessage }}</p>
        </div>
      </aside>
    </section>
  </AppShell>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import AppShell from '../components/AppShell.vue'
import KnowledgeBaseSummary from '../components/KnowledgeBaseSummary.vue'
import { debugKnowledge } from '../api/travel'
import { apiStatus } from '../store/apiStatus'
import type { KnowledgeDebugResponse, KnowledgeReference, KnowledgeSearchResult } from '../types'
import { copyTextToClipboard } from '../utils/clipboard'
import { clonePlanRequest, createDefaultPlanRequest } from '../utils/planRequest'

const form = reactive(createDefaultPlanRequest())
const loading = ref(false)
const hasSearched = ref(false)
const validationError = ref('')
const preferenceInput = ref('')
const avoidInput = ref('')
const copyMessage = ref('')
const responseCopyMessage = ref('')
const results = ref<KnowledgeSearchResult[]>([])
const debugResponse = ref<KnowledgeDebugResponse | null>(null)

const knowledgeReferences = computed<KnowledgeReference[]>(() =>
  results.value.map((item) => ({
    title: item.title,
    source: item.source,
    snippet: item.content,
    score: item.score,
  })),
)

const averageScore = computed(() => {
  if (results.value.length === 0) {
    return '0.00'
  }
  const total = results.value.reduce((sum, item) => sum + item.score, 0)
  return (total / results.value.length).toFixed(2)
})

const topScore = computed(() => {
  if (results.value.length === 0) {
    return 0
  }
  return Math.max(...results.value.map((item) => item.score))
})

const queryPreview = computed(() => {
  const preferences = form.preferences.length > 0 ? form.preferences.join('、') : '暂无偏好'
  const avoid = form.avoid.length > 0 ? form.avoid.join('、') : '暂无避坑项'
  return `${form.departureCity} 出发，前往 ${form.destination}，${form.days} 天，${form.travelers} 人，预算 ${form.budget} 元；偏好：${preferences}；避免：${avoid}。`
})

const requestJson = computed(() => JSON.stringify(clonePlanRequest(form), null, 2))
const responseJson = computed(() => JSON.stringify(debugResponse.value ?? results.value, null, 2))
const debugDestinationKey = computed(() => debugResponse.value?.destinationKey ?? '等待检索')
const debugRetrievalMode = computed(() => debugResponse.value?.retrievalMode ?? '等待检索')
const debugDedicatedLabel = computed(() => {
  if (!debugResponse.value) {
    return '等待检索'
  }
  return debugResponse.value.dedicatedKnowledgeBase ? '是' : '否'
})
const debugVectorStoreLabel = computed(() => {
  if (!debugResponse.value) {
    return '等待检索'
  }
  return debugResponse.value.vectorStoreEnabled ? '已启用' : '未启用'
})
const debugQuery = computed(() => debugResponse.value?.query || '运行检索后展示后端实际用于召回的 query。')

function addUniqueValue(list: string[], value: string) {
  const normalizedValue = value.trim()
  if (normalizedValue && !list.includes(normalizedValue)) {
    list.push(normalizedValue)
  }
}

function addPreference() {
  addUniqueValue(form.preferences, preferenceInput.value)
  preferenceInput.value = ''
}

function addAvoid() {
  addUniqueValue(form.avoid, avoidInput.value)
  avoidInput.value = ''
}

function removePreference(value: string) {
  form.preferences = form.preferences.filter((item) => item !== value)
}

function removeAvoid(value: string) {
  form.avoid = form.avoid.filter((item) => item !== value)
}

function fillDestination(destination: string) {
  form.destination = destination
}

function fillGuangdongExample() {
  form.departureCity = '广州'
  form.destination = '深圳'
  form.days = 2
  form.travelers = 5
  form.budget = 3000
  form.preferences = ['滨海', '文艺', '美食']
  form.avoid = ['频繁换酒店']
}

async function copyRequestJson() {
  copyMessage.value = ''
  try {
    await copyTextToClipboard(requestJson.value)
    copyMessage.value = '已复制请求体 JSON'
  } catch {
    copyMessage.value = '复制失败，请稍后重试'
  }
}

async function copyResponseJson() {
  responseCopyMessage.value = ''
  try {
    await copyTextToClipboard(responseJson.value)
    responseCopyMessage.value = '已复制响应体 JSON'
  } catch {
    responseCopyMessage.value = '复制失败，请稍后重试'
  }
}

async function runSearch() {
  validationError.value = ''

  if (!form.departureCity.trim() || !form.destination.trim()) {
    validationError.value = '请填写出发地和目的地'
    return
  }
  if (form.days < 1 || form.travelers < 1 || form.budget < 500) {
    validationError.value = '天数、人数和预算需要填写合理数值'
    return
  }

  loading.value = true
  hasSearched.value = true
  debugResponse.value = null
  try {
    debugResponse.value = await debugKnowledge(clonePlanRequest(form))
    results.value = debugResponse.value.results
  } catch (error) {
    validationError.value = error instanceof Error ? error.message : '知识库检索失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>
