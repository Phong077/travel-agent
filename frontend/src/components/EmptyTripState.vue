<template>
  <section class="empty-trip-card">
    <div class="empty-trip-icon">
      <span class="material-symbols-outlined">{{ icon }}</span>
    </div>
    <p class="section-eyebrow">暂无行程结果</p>
    <h1 class="section-title">{{ title }}</h1>
    <p class="section-copy">{{ description }}</p>
    <RouterLink class="primary-action" to="/plan">
      <span class="material-symbols-outlined">add</span>
      <span>新建行程</span>
    </RouterLink>
  </section>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    title?: string
    description?: string
    icon?: string
  }>(),
  {
    title: '先生成一份旅行计划',
    description: '填写目的地、预算和旅行偏好后，系统会检索知识库并生成结构化行程。',
    icon: 'travel_explore',
  },
)
</script>
