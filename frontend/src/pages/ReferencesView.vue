<template>
  <AppShell>
    <section>
      <p class="section-eyebrow">RAG References</p>
      <h1 class="section-title">知识库引用来源</h1>
      <p class="section-copy">
        这里展示旅行计划引用了哪些本地知识资料。它不是直接给用户看“模型想了什么”，而是解释结果依据来自哪里。
      </p>
    </section>

    <section class="reference-layout" style="margin-top: 28px">
      <aside class="reference-list">
        <button
          v-for="reference in result.references"
          :key="reference.title + reference.source"
          class="reference-list-card"
          :class="{ active: reference.title === selected.title }"
          type="button"
          @click="selectedTitle = reference.title"
        >
          <span class="score-pill">分数 {{ reference.score }}</span>
          <h2 class="section-title" style="font-size: 18px; margin-top: 12px">{{ reference.title }}</h2>
          <p class="source-line">{{ reference.source }}</p>
          <p class="section-copy" style="font-size: 13px">{{ reference.snippet }}</p>
        </button>
      </aside>

      <article class="reference-preview">
        <div class="reference-preview-header">
          <div style="display: flex; align-items: center; gap: 14px">
            <div class="source-icon">
              <span class="material-symbols-outlined">article</span>
            </div>
            <div>
              <p class="source-line">{{ selected.source }}</p>
              <h2 class="section-title" style="font-size: 24px; margin: 0">{{ selected.title }}</h2>
            </div>
          </div>
          <button class="ghost-action" type="button">查看原文</button>
        </div>

        <div style="margin-top: 34px">
          <p class="section-eyebrow">提取内容</p>
          <p class="section-copy" style="font-size: 16px">{{ selected.snippet }}</p>
        </div>

        <div style="margin-top: 30px">
          <p class="section-eyebrow">被用于生成</p>
          <div class="mini-list">
            <div class="mini-row">
              <span>补充目的地背景和适合人群</span>
              <strong>行程总结</strong>
            </div>
            <div class="mini-row">
              <span>辅助排序景点、美食与交通建议</span>
              <strong>每日安排</strong>
            </div>
          </div>
        </div>

        <div class="bar-row" style="margin-top: 30px">
          <span>匹配分数</span>
          <strong>{{ selected.score }}</strong>
          <div class="bar"><span :style="{ width: `${Math.min(selected.score * 24, 100)}%` }"></span></div>
        </div>
      </article>
    </section>
  </AppShell>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import AppShell from '../components/AppShell.vue'
import { tripState } from '../store/tripStore'

const result = computed(() => tripState.result)
const selectedTitle = ref(result.value.references[0]?.title ?? '')
const fallbackReference = {
  title: '暂无引用来源',
  source: 'knowledge',
  snippet: '当前行程结果没有返回 references。请确认后端 RAG 检索是否正常，或重新生成行程。',
  score: 0,
}
const selected = computed(() => {
  return result.value.references.find((item) => item.title === selectedTitle.value) ?? result.value.references[0] ?? fallbackReference
})
</script>
