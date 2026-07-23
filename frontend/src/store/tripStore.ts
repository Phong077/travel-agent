import { reactive } from 'vue'
import { mockTripPlan } from '../mock'
import type { PlanTripRequest, TripPlanResponse } from '../types'
import { clonePlanRequest, createDefaultPlanRequest } from '../utils/planRequest'
import { readSessionJson } from '../utils/storage'
import { normalizeTripPlanResponse } from '../utils/tripResult'

export type GenerationMode = 'stable' | 'agent' | 'react-agent' | 'multi-agent'

export interface TripHistoryItem {
  id: string
  createdAt: string
  request: PlanTripRequest
  result: TripPlanResponse
  generationMode: GenerationMode
}

interface TripState {
  request: PlanTripRequest
  result: TripPlanResponse
  generationMode: GenerationMode
  history: TripHistoryItem[]
  hasResult: boolean
  loading: boolean
  error: string
}

const HISTORY_KEY = 'travel-agent-history'
const SESSION_VERSION_KEY = 'travel-agent-session-version'
const SESSION_VERSION = '2'

// 当前结果和当前请求属于临时会话数据。升级数据结构后清理旧快照，避免旧目的地被误当成新结果。
if (window.sessionStorage.getItem(SESSION_VERSION_KEY) !== SESSION_VERSION) {
  window.sessionStorage.removeItem('travel-agent-result')
  window.sessionStorage.removeItem('travel-agent-request')
  window.sessionStorage.removeItem('travel-agent-generation-mode')
  window.sessionStorage.setItem(SESSION_VERSION_KEY, SESSION_VERSION)
}

const savedResult = readSessionJson<Partial<TripPlanResponse>>('travel-agent-result')
const savedRequest = readSessionJson<Partial<PlanTripRequest>>('travel-agent-request')
const savedGenerationMode = window.sessionStorage.getItem('travel-agent-generation-mode')

function normalizeGenerationMode(value: unknown): GenerationMode {
  if (value === 'agent' || value === 'react-agent' || value === 'multi-agent') {
    return value
  }

  return 'stable'
}

function readHistory(): TripHistoryItem[] {
  const rawValue = window.localStorage.getItem(HISTORY_KEY)

  if (!rawValue) {
    return []
  }

  try {
    const parsedValue = JSON.parse(rawValue) as Array<Partial<TripHistoryItem>>
    return parsedValue
      .filter((item) => item.id && item.createdAt && item.request && item.result)
      .map((item) => ({
        id: String(item.id),
        createdAt: String(item.createdAt),
        request: clonePlanRequest(item.request as Partial<PlanTripRequest>),
        result: normalizeTripPlanResponse(item.result as Partial<TripPlanResponse>),
        generationMode: normalizeGenerationMode(item.generationMode),
      }))
  } catch {
    window.localStorage.removeItem(HISTORY_KEY)
    return []
  }
}

function writeHistory(history: TripHistoryItem[]) {
  window.localStorage.setItem(HISTORY_KEY, JSON.stringify(history))
}

export const tripState = reactive<TripState>({
  request: savedRequest ? clonePlanRequest(savedRequest) : createDefaultPlanRequest(),
  result: savedResult ? normalizeTripPlanResponse(savedResult) : mockTripPlan,
  generationMode: normalizeGenerationMode(savedGenerationMode),
  history: readHistory(),
  hasResult: Boolean(savedResult),
  loading: false,
  error: '',
})

export function setTripRequest(request: PlanTripRequest) {
  tripState.request = clonePlanRequest(request)
  window.sessionStorage.setItem('travel-agent-request', JSON.stringify(tripState.request))
}

export function setTripResult(result: TripPlanResponse) {
  tripState.result = normalizeTripPlanResponse(result)
  tripState.hasResult = true
  window.sessionStorage.setItem('travel-agent-result', JSON.stringify(tripState.result))
  addTripHistoryItem()
}

export function setGenerationMode(mode: GenerationMode) {
  tripState.generationMode = mode
  window.sessionStorage.setItem('travel-agent-generation-mode', mode)
}

export function resetTripSession() {
  tripState.request = createDefaultPlanRequest()
  tripState.result = mockTripPlan
  tripState.generationMode = 'stable'
  tripState.hasResult = false
  tripState.loading = false
  tripState.error = ''
  window.sessionStorage.removeItem('travel-agent-request')
  window.sessionStorage.removeItem('travel-agent-result')
  window.sessionStorage.removeItem('travel-agent-generation-mode')
  window.sessionStorage.setItem(SESSION_VERSION_KEY, SESSION_VERSION)
}

export function loadTripHistoryItem(id: string) {
  const item = tripState.history.find((historyItem) => historyItem.id === id)
  if (!item) {
    return false
  }

  tripState.request = clonePlanRequest(item.request)
  tripState.result = normalizeTripPlanResponse(item.result)
  tripState.generationMode = item.generationMode
  tripState.hasResult = true
  tripState.error = ''
  window.sessionStorage.setItem('travel-agent-request', JSON.stringify(tripState.request))
  window.sessionStorage.setItem('travel-agent-result', JSON.stringify(tripState.result))
  window.sessionStorage.setItem('travel-agent-generation-mode', tripState.generationMode)
  return true
}

export function deleteTripHistoryItem(id: string) {
  tripState.history = tripState.history.filter((item) => item.id !== id)
  writeHistory(tripState.history)
}

export function clearTripHistory() {
  tripState.history = []
  window.localStorage.removeItem(HISTORY_KEY)
}

function addTripHistoryItem() {
  const historyItem: TripHistoryItem = {
    id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    createdAt: new Date().toISOString(),
    request: clonePlanRequest(tripState.request),
    result: normalizeTripPlanResponse(tripState.result),
    generationMode: tripState.generationMode,
  }

  tripState.history = [historyItem, ...tripState.history].slice(0, 12)
  writeHistory(tripState.history)
}
