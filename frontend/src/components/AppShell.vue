<template>
  <div class="app-shell">
    <aside class="sidebar">
      <RouterLink class="brand" to="/">
        <span class="brand-title">Travel AI</span>
        <span class="brand-subtitle">你的私人旅行规划师</span>
      </RouterLink>

      <RouterLink class="primary-action" to="/plan">
        <span class="material-symbols-outlined">add</span>
        <span>新建行程</span>
      </RouterLink>

      <nav class="nav-stack">
        <RouterLink class="nav-item" to="/">
          <span class="material-symbols-outlined">dashboard</span>
          <span>仪表盘</span>
        </RouterLink>
        <RouterLink class="nav-item" to="/result">
          <span class="material-symbols-outlined">map</span>
          <span>计划</span>
        </RouterLink>
        <RouterLink class="nav-item" to="/knowledge">
          <span class="material-symbols-outlined">menu_book</span>
          <span>知识库</span>
        </RouterLink>
        <RouterLink class="nav-item" to="/references">
          <span class="material-symbols-outlined">hub</span>
          <span>来源</span>
        </RouterLink>
      </nav>

      <div class="sidebar-footer">
        <span>帮助中心</span>
        <div class="user-chip">
          <img
            class="avatar"
            alt="用户头像"
            src="https://lh3.googleusercontent.com/aida-public/AB6AXuD2CtHFVYa6MBg9dRsSKRn0tSwztQDvHDW-yxkZr9Xjqa-46gQoyQd6x50JVxqJIRFamLV3vaI7zREpIocYrODlhPVbIF46QNZvvysaJvbgu_paanJZisGmakk6_rAR21erPWZ5FpX5ZvaVGfVV_PPHT4vhDWecLfFUjdZl69KK2IXTUnjyK6QsI5NRToJmu_Lu9JtW-pYXtNtN3V3A9o99tx0xkz_Xz4C6pg-cauh_yfBldXMyYZlx"
          />
          <span>Kristin Watson</span>
        </div>
      </div>
    </aside>

    <main class="page">
      <div class="mobile-brand-row">
        <div>
          <strong>旅游AI</strong>
          <span>智能旅行规划助手</span>
        </div>
        <img
          class="avatar"
          alt="用户头像"
          src="https://lh3.googleusercontent.com/aida-public/AB6AXuD2CtHFVYa6MBg9dRsSKRn0tSwztQDvHDW-yxkZr9Xjqa-46gQoyQd6x50JVxqJIRFamLV3vaI7zREpIocYrODlhPVbIF46QNZvvysaJvbgu_paanJZisGmakk6_rAR21erPWZ5FpX5ZvaVGfVV_PPHT4vhDWecLfFUjdZl69KK2IXTUnjyK6QsI5NRToJmu_Lu9JtW-pYXtNtN3V3A9o99tx0xkz_Xz4C6pg-cauh_yfBldXMyYZlx"
        />
      </div>

      <header class="topbar">
        <div class="search-pill">
          <span class="material-symbols-outlined">search</span>
          <span>你想去哪里？</span>
        </div>
        <nav class="topbar-tabs">
          <RouterLink to="/result">行程</RouterLink>
          <RouterLink to="/references">发现</RouterLink>
        </nav>
        <button class="icon-button" aria-label="通知">
          <span class="material-symbols-outlined">notifications</span>
        </button>
        <button class="icon-button" aria-label="消息">
          <span class="material-symbols-outlined">chat_bubble</span>
        </button>
        <RouterLink class="primary-action" to="/plan">创建</RouterLink>
      </header>

      <div v-if="apiStatus.usingMock" class="api-status-banner">
        <span class="material-symbols-outlined">info</span>
        <span>{{ apiStatus.message }}</span>
      </div>

      <slot />
    </main>

    <nav class="bottom-nav">
      <RouterLink to="/">
        <span class="material-symbols-outlined">dashboard</span>
        <small>仪表盘</small>
      </RouterLink>
      <RouterLink to="/result">
        <span class="material-symbols-outlined">map</span>
        <small>计划</small>
      </RouterLink>
      <RouterLink to="/knowledge">
        <span class="material-symbols-outlined">menu_book</span>
        <small>知识库</small>
      </RouterLink>
      <RouterLink to="/references">
        <span class="material-symbols-outlined">settings</span>
        <small>设置</small>
      </RouterLink>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { apiStatus } from '../store/apiStatus'
</script>
