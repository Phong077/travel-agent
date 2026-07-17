import type { ApiResponse, HealthStatus, KnowledgeSearchResult, PlanTripRequest, TripPlanResponse } from '../types'
import { createMockKnowledgeResults, createMockTripPlan } from '../mock'
import { markApiFallback, markApiReady } from '../store/apiStatus'

const PLAN_TIMEOUT_MS = 45_000
const SEARCH_TIMEOUT_MS = 12_000
const HEALTH_TIMEOUT_MS = 3_000
const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/$/, '')
const DEV_BACKEND_TARGET = import.meta.env.VITE_BACKEND_PROXY_TARGET || 'http://localhost:8092'

function buildApiUrl(path: string) {
  return `${API_BASE_URL}${path}`
}

async function postJson<T>(url: string, body: unknown, timeoutMs: number): Promise<T> {
  const controller = new AbortController()
  const timeoutId = window.setTimeout(() => controller.abort(), timeoutMs)

  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
      signal: controller.signal,
    })

    if (!response.ok) {
      throw new Error(await buildHttpErrorMessage(response))
    }

    return parseApiPayload<T>(await response.json())
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') {
      throw new Error(`请求超时：${Math.round(timeoutMs / 1000)} 秒内没有收到后端响应`)
    }

    throw error
  } finally {
    window.clearTimeout(timeoutId)
  }
}

async function getJson<T>(url: string, timeoutMs: number): Promise<T> {
  const controller = new AbortController()
  const timeoutId = window.setTimeout(() => controller.abort(), timeoutMs)

  try {
    const response = await fetch(url, {
      method: 'GET',
      signal: controller.signal,
    })

    if (!response.ok) {
      throw new Error(await buildHttpErrorMessage(response))
    }

    return parseApiPayload<T>(await response.json())
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') {
      throw new Error(`请求超时：${Math.round(timeoutMs / 1000)} 秒内没有收到后端响应`)
    }

    throw error
  } finally {
    window.clearTimeout(timeoutId)
  }
}

function parseApiPayload<T>(payload: unknown): T {
  if (typeof payload === 'object' && payload !== null && 'data' in payload) {
    const wrapped = payload as ApiResponse<T>
    if (wrapped.code !== 0 && wrapped.code !== 200) {
      throw new Error(wrapped.message || '后端返回业务错误')
    }
    return wrapped.data
  }

  return payload as T
}

async function buildHttpErrorMessage(response: Response) {
  const responseText = await response.text().catch(() => '')

  if (!API_BASE_URL && response.status === 500 && response.url.includes('/api/')) {
    return `前端代理无法连接后端服务。请确认 Spring Boot 已启动，并监听 ${DEV_BACKEND_TARGET}。`
  }

  if (responseText) {
    return `请求失败：HTTP ${response.status}，${responseText.slice(0, 120)}`
  }

  return `请求失败：HTTP ${response.status}`
}

function getFallbackReason(error: unknown) {
  return error instanceof Error ? error.message : '未知错误'
}

function shouldUseFrontendMock(error: unknown) {
  if (!(error instanceof Error)) {
    return true
  }

  return (
    error.message.includes('前端代理无法连接后端服务') ||
    error.message.includes('请求超时') ||
    error.message.includes('Failed to fetch') ||
    error.message.includes('NetworkError')
  )
}

function validateTripPlanMatchesRequest(result: TripPlanResponse, request: PlanTripRequest) {
  if (result.destination?.trim() !== request.destination.trim()) {
    throw new Error(`后端返回的目的地“${result.destination ?? ''}”与本次请求“${request.destination}”不一致，请重试。`)
  }

  if (result.totalDays !== request.days) {
    throw new Error(`后端返回的旅行天数与本次请求不一致，请重试。`)
  }
}

export async function checkBackendHealth(): Promise<HealthStatus | null> {
  try {
    const result = await getJson<HealthStatus>(buildApiUrl('/api/health'), HEALTH_TIMEOUT_MS)
    markApiReady(`后端服务 ${result.application} 状态正常，健康检查时间：${result.timestamp}。`)
    return result
  } catch (error) {
    console.warn('后端健康检查失败。', error)
    markApiFallback(`后端健康检查失败，后续生成会在接口不可用时使用前端 mock 数据。原因：${getFallbackReason(error)}。`)
    return null
  }
}

export async function planTrip(request: PlanTripRequest): Promise<TripPlanResponse> {
  try {
    const result = await postJson<TripPlanResponse>(buildApiUrl('/api/trips/plan'), request, PLAN_TIMEOUT_MS)
    validateTripPlanMatchesRequest(result, request)
    markApiReady()
    return result
  } catch (error) {
    if (!shouldUseFrontendMock(error)) {
      throw error
    }
    console.warn('后端暂不可用，使用前端 mock 数据兜底。', error)
    markApiFallback(`后端接口暂不可用，当前展示的是前端 mock 演示数据。原因：${getFallbackReason(error)}。`)
    await new Promise((resolve) => window.setTimeout(resolve, 900))
    return createMockTripPlan(request)
  }
}

export async function planTripWithAgent(request: PlanTripRequest): Promise<TripPlanResponse> {
  try {
    const result = await postJson<TripPlanResponse>(buildApiUrl('/api/agent/trips/plan'), request, PLAN_TIMEOUT_MS)
    validateTripPlanMatchesRequest(result, request)
    markApiReady()
    return result
  } catch (error) {
    if (!shouldUseFrontendMock(error)) {
      throw error
    }
    console.warn('Agent 接口暂不可用，使用前端 mock 数据兜底。', error)
    markApiFallback(`Agent Tool Calling 接口暂不可用，当前展示的是前端 mock 演示数据。原因：${getFallbackReason(error)}。`)
    await new Promise((resolve) => window.setTimeout(resolve, 900))
    return createMockTripPlan(request)
  }
}

export async function searchKnowledge(request: PlanTripRequest): Promise<KnowledgeSearchResult[]> {
  try {
    const result = await postJson<KnowledgeSearchResult[]>(buildApiUrl('/api/knowledge/search'), request, SEARCH_TIMEOUT_MS)
    markApiReady()
    return result
  } catch (error) {
    console.warn('知识库接口暂不可用，使用前端 mock 数据兜底。', error)
    markApiFallback(`知识库接口暂不可用，当前展示的是前端 mock 检索结果。原因：${getFallbackReason(error)}。`)
    return createMockKnowledgeResults(request)
  }
}
