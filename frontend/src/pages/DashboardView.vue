<template>
  <AppShell>
    <section>
      <p class="section-eyebrow">智能旅行助手</p>
      <h1 class="hero-title">你好，Kristin！</h1>
      <p class="section-copy">准备好规划下一次冒险了吗？让 AI 为你安排有依据、有预算、有节奏的四川行程。</p>
    </section>

    <section class="dashboard-grid" style="margin-top: 32px">
      <div>
        <article class="analytics-card travel-showcase-card">
          <div class="showcase-header">
            <div>
              <p class="section-eyebrow">当前推荐</p>
              <h2>探索成都与川西精华</h2>
            </div>
            <span class="score-pill">RAG 5 条引用</span>
          </div>
          <div class="fake-dashboard">
            <div class="metric">
              <span>偏好匹配</span>
              <span class="metric-value">83%</span>
              <small>美食与自然风景</small>
            </div>
            <div class="metric blue">
              <span>知识库命中</span>
              <span class="metric-value">56%</span>
              <small>四川目的地资料</small>
            </div>
            <div class="mini-list" style="grid-column: 1 / -1">
              <div class="mini-row"><span>宽窄巷子</span><strong>下午</strong></div>
              <div class="mini-row"><span>都江堰</span><strong>第 2 天</strong></div>
              <div class="mini-row"><span>乐山大佛</span><strong>第 3 天</strong></div>
            </div>
          </div>
          <div class="line-chart"></div>
          <RouterLink class="primary-action showcase-action" to="/plan">
            <span class="material-symbols-outlined">auto_awesome</span>
            <span>开始计划</span>
          </RouterLink>
        </article>

        <div style="margin-top: 22px">
          <p class="section-eyebrow">推荐路线</p>
          <h2 class="section-title">探索成都与川西精华</h2>
          <p class="section-copy">发现隐藏的美食、茶馆和自然风景，生成更适合预算和偏好的旅行计划。</p>
          <RouterLink class="primary-action" to="/plan" style="margin-top: 18px">
            <span class="material-symbols-outlined">auto_awesome</span>
            <span>开始计划</span>
          </RouterLink>
        </div>
      </div>

      <aside>
        <section class="panel trip-preview-panel">
          <p class="section-eyebrow">即将到来</p>
          <h3>成都轻松游</h3>
          <p class="section-copy">5 天 4 晚 · 2 人 · 预算 6000</p>
          <div class="trip-preview-image">
            <span>京畿缘起</span>
            <strong>14天后</strong>
          </div>
          <div class="bar-row" style="margin-top: 18px">
            <span>准备进度</span>
            <strong>80%</strong>
            <div class="bar"><span style="width: 80%"></span></div>
          </div>
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
          </div>
        </section>
      </aside>
    </section>
  </AppShell>
</template>

<script setup lang="ts">
import AppShell from '../components/AppShell.vue'
</script>
