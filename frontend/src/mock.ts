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

export function createMockKnowledgeResults(request: PlanTripRequest): KnowledgeSearchResult[] {
  const preferenceText = request.preferences.length > 0 ? request.preferences.join('、') : '当地特色'
  const avoidText = request.avoid.length > 0 ? request.avoid.join('、') : '过度奔波'

  return [
    {
      title: `${request.destination}目的地概览`,
      source: 'mock/fallback-destination.md',
      content: `前端 mock 检索结果：当前后端知识库接口不可用，临时根据目的地“${request.destination}”生成概览内容，用于保持调试页演示流程完整。`,
      score: 0,
    },
    {
      title: `${request.destination}偏好匹配`,
      source: 'mock/fallback-preferences.md',
      content: `根据本次偏好“${preferenceText}”，mock 结果会提示后端真实可用时应优先检索景点、美食、交通和季节资料。`,
      score: 0,
    },
    {
      title: `${request.destination}避坑约束`,
      source: 'mock/fallback-rules.md',
      content: `根据避坑项“${avoidText}”，mock 结果会提示生成时应控制行程强度，并避免与用户约束冲突。`,
      score: 0,
    },
  ]
}

const genericDayThemes = ['抵达与城市初印象', '核心景点与本地美食', '自然风光与轻松体验', '文化街区与深度漫游', '返程前的伴手礼']

export function createMockTripPlan(request: PlanTripRequest): TripPlanResponse {
  const perPersonBudget = Math.round(request.budget / request.travelers)
  const perPersonDailyBudget = Math.round(perPersonBudget / request.days)
  const days = Array.from({ length: request.days }, (_, index) => {
    const day = index + 1
    const theme = genericDayThemes[index] ?? `第 ${day} 天弹性探索`

    return {
      day,
      theme: `${request.destination}${theme}`,
      morning:
        day === 1
          ? `从${request.departureCity}出发前往${request.destination}，抵达后优先入住交通便利区域，保留休整时间。`
          : `围绕${request.destination}安排一个核心目的地，上午以轻量游览为主，避免行程过早开始。`,
      afternoon: `结合${request.preferences.join('、') || '当地特色'}安排体验，优先选择交通顺路、节奏稳定的地点。`,
      evening: `晚间安排本地餐饮和轻松散步，避开${request.avoid.join('、') || '过度奔波'}。`,
      transportTip: `这是前端 mock 兜底行程。真实交通建议需要后端接口返回，当前建议以公共交通和短距离步行为主。`,
    }
  })

  return {
    destination: request.destination,
    totalDays: request.days,
    summary: `这是根据“${request.destination}”生成的前端 mock 兜底行程，用于后端暂不可用时保持页面演示完整。真实行程会由 Spring Boot 后端结合 RAG 知识库生成。`,
    budgetAnalysis: {
      perPersonBudget,
      perPersonDailyBudget,
      level: perPersonDailyBudget >= 1000 ? '舒适' : perPersonDailyBudget >= 500 ? '适中' : '经济',
      suggestion: `当前预算约为人均 ${perPersonBudget} 元、每日人均 ${perPersonDailyBudget} 元。真实预算建议以后端 BudgetService 返回为准。`,
    },
    days,
    references: [
      {
        title: `${request.destination} mock 引用`,
        source: 'mock/fallback.md',
        snippet: '当前后端接口暂不可用，因此前端使用 mock 数据保持演示流程可用。',
        score: 0,
      },
    ],
  }
}
