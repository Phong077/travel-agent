<template>
  <AppShell>
    <EmptyTripState
      v-if="!hasResult"
      title="还没有每日行程"
      description="生成旅行计划后，系统会在这里按天拆分上午、下午、晚上和交通建议。"
      icon="route"
    />

    <template v-else>
    <section>
      <p class="section-eyebrow">每日行程详情</p>
      <h1 class="section-title">探索{{ result.destination }}</h1>
      <p class="section-copy">按天拆分上午、下午、晚上与交通建议，方便用户快速判断行程节奏。</p>
    </section>

    <section class="day-detail-shell" style="margin-top: 28px">
      <aside class="day-selector panel">
        <p class="section-eyebrow">行程导航</p>
        <button
          v-for="day in result.days"
          :key="day.day"
          class="day-selector-item"
          :class="{ active: expandedDay === day.day }"
          type="button"
          @click="expandedDay = day.day"
        >
          <span>Day {{ day.day }}</span>
          <strong>{{ day.theme }}</strong>
        </button>
      </aside>

      <div class="timeline-list">
        <article
          v-for="day in result.days"
          :key="day.day"
          class="timeline-card day-accordion"
          :class="{ collapsed: expandedDay !== day.day }"
        >
          <button class="day-accordion-header" type="button" @click="toggleDay(day.day)">
            <div class="day-index">{{ day.day }}</div>
            <div>
              <p class="section-eyebrow">Day {{ day.day }} · {{ getDayDistance(day.day) }}</p>
              <h2 class="section-title" style="font-size: 22px">{{ day.theme }}</h2>
            </div>
            <span class="material-symbols-outlined">{{ expandedDay === day.day ? 'expand_less' : 'expand_more' }}</span>
          </button>

          <div v-if="expandedDay === day.day" class="day-accordion-body">
            <div class="day-map-card">
              <img alt="行程地图预览" src="https://lh3.googleusercontent.com/aida-public/AB6AXuBJwcM_SkBwEeKShHhNoCHLkhZmXhlLoMPYCDwAcKQJVzD2nsN6DJxsq-C2Eo1UFUo4FHs7haZeN8fvUh5qe6VcJ8Ic62lKdEU1Y5UYgXSiAGOFJRJLQUx-s2VnvqYipFxlzEb-nKQEjvSgHhHbb1i5E5rKDqmM-ITUZ53NwYJfeksnpIU1a3tzdVIFVlS5pGx5-BWDhML8S2kzQsCTeXk9J_Jw2_XCbIwC3OBHQ_P9Ru0R-4ezNxsQW5L01hhhA1mL3g" />
              <div>
                <span class="hero-badge">路线预览</span>
                <strong>{{ day.theme }}</strong>
              </div>
            </div>

            <div class="day-step-list">
              <div class="day-step">
                <div class="step-dot"></div>
                <div class="time-block">
                  <strong>上午</strong>
                  <span>{{ day.morning }}</span>
                </div>
              </div>
              <div class="day-step">
                <div class="step-dot"></div>
                <div class="time-block">
                  <strong>下午</strong>
                  <span>{{ day.afternoon }}</span>
                </div>
              </div>
              <div class="day-step">
                <div class="step-dot"></div>
                <div class="time-block">
                  <strong>晚上</strong>
                  <span>{{ day.evening }}</span>
                </div>
              </div>
            </div>

            <div class="transport-tip">
              <strong>交通建议：</strong>{{ day.transportTip }}
            </div>

            <div v-if="getWeatherTip(day.day)" class="daily-weather-tip">
              <span class="material-symbols-outlined">wb_cloudy</span>
              <div>
                <strong>当日天气提醒</strong>
                <p>{{ getWeatherTip(day.day) }}</p>
              </div>
            </div>

            <div class="day-actions">
              <button class="ghost-action" type="button" @click="copyDay(day.day)">复制当天安排</button>
              <RouterLink class="primary-action" to="/references">查看引用来源</RouterLink>
            </div>
            <p v-if="copyMessageByDay[day.day]" class="inline-feedback">{{ copyMessageByDay[day.day] }}</p>
          </div>
        </article>
      </div>
    </section>
    </template>
  </AppShell>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import AppShell from '../components/AppShell.vue'
import EmptyTripState from '../components/EmptyTripState.vue'
import { tripState } from '../store/tripStore'
import { copyTextToClipboard } from '../utils/clipboard'
import { formatItineraryDay } from '../utils/tripFormatter'

const hasResult = computed(() => tripState.hasResult)
const result = computed(() => tripState.result)
const expandedDay = ref(1)
const copyMessageByDay = reactive<Record<number, string>>({})

function toggleDay(day: number) {
  expandedDay.value = day
}

function getDayDistance(day: number) {
  const distances = ['轻松抵达', '核心游览', '深度体验', '弹性安排', '返程收尾']
  return distances[day - 1] ?? '轻松路线'
}

function getWeatherTip(day: number) {
  return result.value.weatherInfo?.dailyTips?.[day - 1] ?? ''
}

async function copyDay(dayNumber: number) {
  const day = result.value.days.find((item) => item.day === dayNumber)
  if (!day) {
    return
  }

  copyMessageByDay[dayNumber] = ''
  try {
    await copyTextToClipboard(formatItineraryDay(result.value.destination, day, getWeatherTip(dayNumber)))
    copyMessageByDay[dayNumber] = '已复制当天安排'
  } catch {
    copyMessageByDay[dayNumber] = '复制失败，请稍后重试'
  }
}
</script>
