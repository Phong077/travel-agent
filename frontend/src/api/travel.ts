import type { ApiResponse, KnowledgeSearchResult, PlanTripRequest, TripPlanResponse } from '../types'
import { mockKnowledgeResults, mockTripPlan } from '../mock'
import { markApiFallback, markApiReady } from '../store/apiStatus'

async function postJson<T>(url: string, body: unknown): Promise<T> {
  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  })

  if (!response.ok) {
    throw new Error(`请求失败：HTTP ${response.status}`)
  }

  const payload = (await response.json()) as ApiResponse<T> | T
  if (typeof payload === 'object' && payload !== null && 'data' in payload) {
    const wrapped = payload as ApiResponse<T>
    if (wrapped.code !== 0 && wrapped.code !== 200) {
      throw new Error(wrapped.message || '后端返回业务错误')
    }
    return wrapped.data
  }

  return payload as T
}

export async function planTrip(request: PlanTripRequest): Promise<TripPlanResponse> {
  try {
    const result = await postJson<TripPlanResponse>('/api/trips/plan', request)
    markApiReady()
    return result
  } catch (error) {
    console.warn('后端暂不可用，使用前端 mock 数据兜底。', error)
    markApiFallback('后端接口暂不可用，当前展示的是前端 mock 演示数据。启动 Spring Boot 后会自动调用真实接口。')
    await new Promise((resolve) => window.setTimeout(resolve, 900))
    return mockTripPlan
  }
}

export async function searchKnowledge(request: PlanTripRequest): Promise<KnowledgeSearchResult[]> {
  try {
    const result = await postJson<KnowledgeSearchResult[]>('/api/knowledge/search', request)
    markApiReady()
    return result
  } catch (error) {
    console.warn('知识库接口暂不可用，使用前端 mock 数据兜底。', error)
    markApiFallback('知识库接口暂不可用，当前展示的是前端 mock 检索结果。启动后端后可验证真实 RAG 检索。')
    return mockKnowledgeResults
  }
}
