<template>
  <AppShell>
    <section>
      <p class="section-eyebrow">AI 旅行</p>
      <h1 class="section-title">设计您的下一次冒险</h1>
      <p class="section-copy">告诉我们您在哪里出发、偏好什么，我们会从知识库检索资料并生成结构化行程。</p>
    </section>

    <form class="form-grid" style="margin-top: 28px" @submit.prevent="submitPlan">
      <div>
        <section class="form-card">
          <h2 class="section-title" style="font-size: 20px">基础信息</h2>
          <div class="field-grid">
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
                <input v-model="form.destination" placeholder="四川 / 云南 / 新疆" />
              </div>
            </label>
            <label class="field">
              <span>日期</span>
              <div class="field-control">
                <span class="material-symbols-outlined">calendar_month</span>
                <input value="未来 1 个月内" disabled />
              </div>
            </label>
            <label class="field">
              <span>时长（天）</span>
              <div class="field-control">
                <span class="material-symbols-outlined">date_range</span>
                <input v-model.number="form.days" type="number" min="1" max="15" />
              </div>
            </label>
            <label class="field">
              <span>出行人数</span>
              <div class="field-control">
                <span class="material-symbols-outlined">group</span>
                <input v-model.number="form.travelers" type="number" min="1" max="12" />
              </div>
            </label>
            <label class="field">
              <span>总预算</span>
              <div class="field-control">
                <span class="material-symbols-outlined">payments</span>
                <input v-model.number="form.budget" type="number" min="500" step="100" />
              </div>
            </label>
          </div>
          <div v-if="validationError" class="error-banner">{{ validationError }}</div>
        </section>

        <section class="form-card" style="margin-top: 24px">
          <h2 class="section-title" style="font-size: 20px">旅行偏好</h2>
          <div class="preference-grid">
            <button
              v-for="item in preferenceOptions"
              :key="item"
              type="button"
              class="chip"
              :class="{ selected: form.preferences.includes(item) }"
              @click="toggleValue(form.preferences, item)"
            >
              {{ item }}
            </button>
          </div>
          <div class="plan-custom-row">
            <div class="debug-add-control">
              <input v-model="preferenceInput" placeholder="添加自定义偏好，例如：摄影、温泉" @keydown.enter.prevent="addPreference" />
              <button class="ghost-action" type="button" @click="addPreference">添加</button>
            </div>
          </div>
          <div v-if="form.preferences.length > 0" class="selected-chip-panel">
            <p class="section-eyebrow">已选偏好</p>
            <div class="preference-grid">
              <button v-for="item in form.preferences" :key="item" type="button" class="chip selected" @click="removeValue(form.preferences, item)">
                {{ item }}
                <span class="material-symbols-outlined">close</span>
              </button>
            </div>
          </div>

          <h2 class="section-title" style="font-size: 20px; margin-top: 28px">尽量避免</h2>
          <div class="preference-grid">
            <button
              v-for="item in avoidOptions"
              :key="item"
              type="button"
              class="chip"
              :class="{ danger: form.avoid.includes(item) }"
              @click="toggleValue(form.avoid, item)"
            >
              {{ item }}
            </button>
          </div>
          <div class="plan-custom-row">
            <div class="debug-add-control">
              <input v-model="avoidInput" placeholder="添加避坑项，例如：夜车、排队太久" @keydown.enter.prevent="addAvoid" />
              <button class="ghost-action" type="button" @click="addAvoid">添加</button>
            </div>
          </div>
          <div v-if="form.avoid.length > 0" class="selected-chip-panel">
            <p class="section-eyebrow">已选避坑项</p>
            <div class="preference-grid">
              <button v-for="item in form.avoid" :key="item" type="button" class="chip danger" @click="removeValue(form.avoid, item)">
                {{ item }}
                <span class="material-symbols-outlined">close</span>
              </button>
            </div>
          </div>
        </section>
      </div>

      <aside class="panel">
        <p class="section-eyebrow">生成模式</p>
        <div class="generation-mode-group">
          <button
            v-for="mode in generationModes"
            :key="mode.value"
            type="button"
            class="generation-mode-card"
            :class="{ active: selectedGenerationMode === mode.value }"
            @click="selectedGenerationMode = mode.value"
          >
            <span>{{ mode.name }}</span>
            <strong>{{ mode.title }}</strong>
            <small>{{ mode.description }}</small>
          </button>
        </div>

        <p class="section-eyebrow">预算风格</p>
        <div class="mini-list">
          <button
            v-for="style in budgetStyles"
            :key="style.name"
            type="button"
            class="budget-style-card"
            :class="{ active: selectedBudgetStyle === style.name }"
            @click="selectedBudgetStyle = style.name"
          >
            <span>{{ style.name }}</span>
            <strong>{{ style.price }}</strong>
            <small>{{ style.description }}</small>
          </button>
        </div>
      </aside>

      <div class="page-actions" style="grid-column: 1 / -1">
        <RouterLink class="ghost-action" to="/">返回首页</RouterLink>
        <button class="ghost-action" type="button" @click="resetForm">重置表单</button>
        <button class="primary-action" type="submit" :disabled="tripState.loading">
          <span class="material-symbols-outlined">auto_awesome</span>
          <span>{{ tripState.loading ? '生成中' : '生成 AI 行程' }}</span>
        </button>
      </div>
    </form>
  </AppShell>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import AppShell from '../components/AppShell.vue'
import { planTrip, planTripWithAgent } from '../api/travel'
import { setGenerationMode, setTripRequest, setTripResult, tripState, type GenerationMode } from '../store/tripStore'
import { clonePlanRequest, createDefaultPlanRequest } from '../utils/planRequest'

const router = useRouter()

const form = reactive(clonePlanRequest(tripState.request))
const preferenceOptions = ['自然风光', '美食探索', '文化艺术', '轻松节奏', '购物', '亲子友好']
const avoidOptions = ['太早起床', '频繁换酒店', '长途爬山', '排队太久']
const selectedBudgetStyle = ref('舒适')
const selectedGenerationMode = ref<GenerationMode>(tripState.generationMode)
const validationError = ref('')
const preferenceInput = ref('')
const avoidInput = ref('')
const budgetStyles = [
  { name: '经济', price: '¥', description: '控制住宿和交通成本' },
  { name: '舒适', price: '¥¥', description: '兼顾体验与预算' },
  { name: '奢华', price: '¥¥¥', description: '更好的酒店和餐饮' },
]
const generationModes: Array<{
  value: GenerationMode
  name: string
  title: string
  description: string
}> = [
  {
    value: 'stable',
    name: '稳定版',
    title: '服务编排',
    description: '后端固定调用知识库、预算和天气服务，更稳定可控。',
  },
  {
    value: 'agent',
    name: 'Agent 版',
    title: 'Tool Calling',
    description: '大模型通过工具调用预算、天气和知识库能力。',
  },
]

function getDefaultForm() {
  return createDefaultPlanRequest()
}

function toggleValue(list: string[], value: string) {
  const index = list.indexOf(value)
  if (index >= 0) {
    list.splice(index, 1)
  } else {
    list.push(value)
  }
}

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

function removeValue(list: string[], value: string) {
  const index = list.indexOf(value)
  if (index >= 0) {
    list.splice(index, 1)
  }
}

function resetForm() {
  validationError.value = ''
  Object.assign(form, getDefaultForm())
  selectedBudgetStyle.value = '舒适'
  selectedGenerationMode.value = 'stable'
  setGenerationMode('stable')
  preferenceInput.value = ''
  avoidInput.value = ''
}

async function submitPlan() {
  validationError.value = ''
  if (!form.departureCity.trim() || !form.destination.trim()) {
    validationError.value = '请填写出发地和目的地'
    return
  }
  if (form.days < 1 || form.travelers < 1 || form.budget < 500) {
    validationError.value = '天数、人数和预算需要填写合理数值'
    return
  }

  setTripRequest(clonePlanRequest(form))
  setGenerationMode(selectedGenerationMode.value)
  tripState.loading = true
  tripState.error = ''
  await router.push('/loading')

  try {
    const result = selectedGenerationMode.value === 'agent' ? await planTripWithAgent(tripState.request) : await planTrip(tripState.request)
    setTripResult(result)
    await router.push('/result')
  } catch (error) {
    tripState.error = error instanceof Error ? error.message : '生成失败，请稍后重试'
    await router.push('/plan')
  } finally {
    tripState.loading = false
  }
}
</script>
