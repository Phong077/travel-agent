<template>
  <article class="knowledge-base-card">
    <div>
      <p class="section-eyebrow">当前知识库</p>
      <h3>{{ title }}</h3>
      <p class="section-copy">{{ summary }}</p>
    </div>

    <div class="knowledge-base-badges" aria-label="已使用的知识库">
      <span v-if="items.length === 0" class="knowledge-base-badge muted">暂无引用</span>
      <span v-for="item in items" v-else :key="item.key" class="knowledge-base-badge" :class="item.type">
        {{ item.label }}
      </span>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { KnowledgeReference } from '../types'
import { getKnowledgeBaseItems, getKnowledgeBaseSummary, getKnowledgeBaseTitle } from '../utils/knowledgeBase'

const props = defineProps<{
  references: KnowledgeReference[]
}>()

const items = computed(() => getKnowledgeBaseItems(props.references))
const title = computed(() => getKnowledgeBaseTitle(items.value))
const summary = computed(() => getKnowledgeBaseSummary(items.value))
</script>
