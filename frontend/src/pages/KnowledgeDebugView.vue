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
      <form class="form-card" @submit.prevent="runSearch">
        <div class="debug-search-card">
          <span class="material-symbols-outlined">search</span>
          <input v-model="queryText" style="flex: 1; border: 0; background: transparent; outline: none" />
          <span class="score-pill">分数阈值 0.75</span>
          <button class="primary-action" type="submit" style="min-height: 36px">
            {{ loading ? '检索中' : '运行查询' }}
          </button>
        </div>

        <div class="result-strip" style="margin-top: 24px">
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
      </form>

      <aside class="panel">
        <p class="section-eyebrow">查询分析</p>
        <h2 class="section-title" style="font-size: 20px">当前检索链路</h2>
        <div class="preference-grid">
          <span v-for="item in [...form.preferences, form.destination]" :key="item" class="chip selected">{{ item }}</span>
        </div>
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
            <span>使用 Token</span>
            <strong>1024</strong>
            <div class="bar"><span style="width: 58%"></span></div>
          </div>
        </div>
      </aside>
    </section>
  </AppShell>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import AppShell from '../components/AppShell.vue'
import { searchKnowledge } from '../api/travel'
import { defaultRequest, mockKnowledgeResults } from '../mock'
import type { KnowledgeSearchResult } from '../types'

const form = reactive({ ...defaultRequest })
const loading = ref(false)
const results = ref<KnowledgeSearchResult[]>(mockKnowledgeResults)
const queryText = ref('agent travel tips 四川 美食 自然风景')
const averageScore = computed(() => {
  if (results.value.length === 0) {
    return '0.00'
  }
  const total = results.value.reduce((sum, item) => sum + item.score, 0)
  return (total / results.value.length).toFixed(2)
})

async function runSearch() {
  loading.value = true
  form.destination = queryText.value.includes('四川') ? '四川' : form.destination
  try {
    results.value = await searchKnowledge(form)
  } finally {
    loading.value = false
  }
}
</script>
