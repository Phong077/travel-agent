<template>
  <AppShell>
    <section class="trip-hero">
      <div>
        <span class="hero-badge">{{ result.destination }} · {{ result.totalDays }} 天</span>
        <h1 class="hero-title" style="margin-top: 16px">玩转四川</h1>
        <p style="max-width: 720px; line-height: 1.8">{{ result.summary }}</p>
      </div>
      <RouterLink class="ghost-action" to="/plan" style="position: absolute; right: 34px; bottom: 34px">修改需求</RouterLink>
    </section>

    <section class="content-grid">
      <div>
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
        <p class="section-eyebrow">预算概览</p>
        <div class="budget-bars">
          <div class="bar-row">
            <span>人均预算</span>
            <strong>¥{{ result.budgetAnalysis?.perPersonBudget ?? 0 }}</strong>
            <div class="bar"><span style="width: 74%"></span></div>
          </div>
          <div class="bar-row">
            <span>每日人均</span>
            <strong>¥{{ result.budgetAnalysis?.perPersonDailyBudget ?? 0 }}</strong>
            <div class="bar"><span style="width: 62%"></span></div>
          </div>
          <div class="bar-row">
            <span>舒适度</span>
            <strong>{{ result.budgetAnalysis?.level ?? '适中' }}</strong>
            <div class="bar"><span style="width: 68%"></span></div>
          </div>
        </div>
        <p class="section-copy" style="margin-top: 22px">{{ result.budgetAnalysis?.suggestion }}</p>
        <RouterLink class="primary-action" to="/days" style="margin-top: 20px; width: 100%">查看行程</RouterLink>
        <RouterLink class="ghost-action" to="/references" style="margin-top: 12px; width: 100%">查看引用来源</RouterLink>
      </aside>
    </section>
  </AppShell>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import AppShell from '../components/AppShell.vue'
import { tripState } from '../store/tripStore'

const result = computed(() => tripState.result)
const request = computed(() => tripState.request)
</script>
