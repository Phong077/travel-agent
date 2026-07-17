import type { KnowledgeReference } from '../types'

export interface KnowledgeBaseItem {
  key: string
  label: string
  description: string
  type: 'destination' | 'common' | 'mock' | 'unknown'
}

const KNOWLEDGE_BASE_META: Record<string, Omit<KnowledgeBaseItem, 'key'>> = {
  sichuan: {
    label: '四川知识库',
    description: '包含四川景点、美食、季节和交通资料',
    type: 'destination',
  },
  yunnan: {
    label: '云南知识库',
    description: '包含云南景点、美食、季节和交通资料',
    type: 'destination',
  },
  common: {
    label: '通用旅行规则',
    description: '用于补充预算、节奏、避坑和基础规划规则',
    type: 'common',
  },
  mock: {
    label: 'Mock 兜底',
    description: '后端接口不可用时由前端生成的演示数据',
    type: 'mock',
  },
}

function resolveKnowledgeBaseKey(source: string): string {
  const normalizedSource = source.trim().replace(/\\/g, '/')
  const [firstPathSegment] = normalizedSource.split('/')

  if (KNOWLEDGE_BASE_META[firstPathSegment]) {
    return firstPathSegment
  }

  // 兼容旧版知识库文件名，例如 sichuan-food.md。
  const legacyKey = Object.keys(KNOWLEDGE_BASE_META).find((key) => normalizedSource.startsWith(`${key}-`))
  return legacyKey ?? firstPathSegment
}

export function getKnowledgeBaseItems(references: KnowledgeReference[]): KnowledgeBaseItem[] {
  const keys = Array.from(new Set(references.map((reference) => resolveKnowledgeBaseKey(reference.source)).filter(Boolean)))

  return keys.map(getKnowledgeBaseItemByKey)
}

export function getKnowledgeBaseItemBySource(source: string): KnowledgeBaseItem {
  return getKnowledgeBaseItemByKey(resolveKnowledgeBaseKey(source))
}

function getKnowledgeBaseItemByKey(key: string): KnowledgeBaseItem {
  const meta = KNOWLEDGE_BASE_META[key]

  if (meta) {
    return {
      key,
      ...meta,
    }
  }

  return {
    key,
    label: `${key} 知识库`,
    description: '来自后端返回的知识库引用来源',
    type: 'unknown',
  }
}

export function getKnowledgeBaseTitle(items: KnowledgeBaseItem[]): string {
  if (items.length === 0) {
    return '暂无知识库引用'
  }

  const destinationItems = items.filter((item) => item.type === 'destination' || item.type === 'unknown')
  const visibleItems = destinationItems.length > 0 ? destinationItems : items

  return visibleItems.map((item) => item.label).join('、')
}

export function getKnowledgeBaseSummary(items: KnowledgeBaseItem[]): string {
  if (items.length === 0) {
    return '当前结果没有返回 references，暂时无法判断使用了哪一组知识库。'
  }

  const hasCommonRules = items.some((item) => item.key === 'common')
  const hasMockFallback = items.some((item) => item.key === 'mock')
  const destinationItems = items.filter((item) => item.type === 'destination' || item.type === 'unknown')

  if (destinationItems.length === 0 && hasMockFallback) {
    return '当前后端接口暂不可用，本次展示的是前端 Mock 兜底数据。'
  }

  if (destinationItems.length === 0 && hasCommonRules) {
    return '当前目的地暂无专属知识库，本次主要使用通用旅行规则完成兜底规划。'
  }

  const destinationLabels = destinationItems.map((item) => item.label).join('、')
  return hasCommonRules ? `本次使用 ${destinationLabels}，并结合通用旅行规则。` : `本次使用 ${destinationLabels}。`
}
