import type { PlanTripRequest, TripPlanResponse, KnowledgeSearchResult } from './types'

export const defaultRequest: PlanTripRequest = {
  departureCity: '重庆',
  destination: '四川',
  days: 5,
  travelers: 2,
  budget: 6000,
  preferences: ['美食', '自然风景', '轻松节奏'],
  avoid: ['太早起床', '每天换酒店'],
}

export const mockTripPlan: TripPlanResponse = {
  destination: '四川',
  totalDays: 5,
  summary:
    '以成都为基地，轻松漫游川西精华线。行程兼顾城市烟火、美食体验、自然风景与文化景点，全程减少频繁换酒店，适合两人 6000 元预算的休闲旅行。',
  budgetAnalysis: {
    perPersonBudget: 3000,
    perPersonDailyBudget: 600,
    level: '适中',
    suggestion: '预算适合普通舒适型旅行，建议住宿选择交通便利区域，把更多预算留给餐饮和核心景区体验。',
  },
  days: [
    {
      day: 1,
      theme: '成都初印象：美食与城市烟火',
      morning: '从重庆出发乘高铁抵达成都，入住春熙路或宽窄巷子附近酒店，稍作休整。',
      afternoon: '漫步宽窄巷子、人民公园，体验盖碗茶和成都慢生活。',
      evening: '晚餐安排钟水饺、龙抄手或川菜馆，饭后逛锦里古街。',
      transportTip: '重庆到成都高铁约 1.5 小时，成都市内以地铁和步行为主，避免打车拥堵。',
    },
    {
      day: 2,
      theme: '都江堰与青城山：世界遗产一日游',
      morning: '前往都江堰景区，参观鱼嘴、飞沙堰、宝瓶口。',
      afternoon: '游览青城山前山，选择轻松线路，体验幽静山林。',
      evening: '返回成都，安排清淡川菜或小吃，早点休息。',
      transportTip: '建议乘城际铁路或景区直通车，单日往返更省心。',
    },
    {
      day: 3,
      theme: '乐山大佛与地道川菜',
      morning: '从成都前往乐山，游览乐山大佛景区。',
      afternoon: '品尝跷脚牛肉、甜皮鸭、钵钵鸡等乐山美食。',
      evening: '返回成都，夜间安排宽松，避免赶行程。',
      transportTip: '成都东站到乐山高铁约 1 小时，适合当天往返。',
    },
    {
      day: 4,
      theme: '峨眉山低海拔精华段',
      morning: '前往峨眉山，优先选择报国寺、伏虎寺等低海拔区域。',
      afternoon: '漫步清音阁附近路线，控制体力消耗。',
      evening: '返回成都或住峨眉山脚，按预算选择。',
      transportTip: '如果不想换酒店，可当天返回成都；想更从容可住峨眉山脚一晚。',
    },
    {
      day: 5,
      theme: '返程前的成都伴手礼',
      morning: '睡到自然醒，逛太古里或春熙路。',
      afternoon: '购买火锅底料、茶叶、糕点等伴手礼。',
      evening: '乘高铁返回重庆，结束行程。',
      transportTip: '预留 2 小时前往车站，返程日不要安排远距离景点。',
    },
  ],
  references: [
    {
      title: '成都',
      source: 'sichuan-attractions.md',
      snippet: '成都适合作为大多数四川旅行的起点，适合安排美食、城市漫步、茶馆、熊猫基地等轻松行程。',
      score: 3,
    },
    {
      title: '成都作为中转城市',
      source: 'sichuan-transport.md',
      snippet: '成都连接都江堰、乐山、峨眉山等方向较方便，适合作为多日旅行的交通中心。',
      score: 3,
    },
    {
      title: '川菜与小吃',
      source: 'sichuan-food.md',
      snippet: '四川旅行可以结合火锅、串串、川菜馆和地方小吃，餐饮体验是行程亮点之一。',
      score: 2,
    },
  ],
}

export const mockKnowledgeResults: KnowledgeSearchResult[] = mockTripPlan.references.map((item) => ({
  title: item.title,
  source: item.source,
  content: item.snippet,
  score: item.score,
}))
