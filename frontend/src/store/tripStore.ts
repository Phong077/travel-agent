import { reactive } from 'vue'
import { mockTripPlan } from '../mock'
import type { PlanTripRequest, TripPlanResponse } from '../types'
import { clonePlanRequest, createDefaultPlanRequest } from '../utils/planRequest'
import { readSessionJson } from '../utils/storage'
import { normalizeTripPlanResponse } from '../utils/tripResult'

interface TripState {
  request: PlanTripRequest
  result: TripPlanResponse
  hasResult: boolean
  loading: boolean
  error: string
}

const savedResult = readSessionJson<Partial<TripPlanResponse>>('travel-agent-result')
const savedRequest = readSessionJson<Partial<PlanTripRequest>>('travel-agent-request')

export const tripState = reactive<TripState>({
  request: savedRequest ? clonePlanRequest(savedRequest) : createDefaultPlanRequest(),
  result: savedResult ? normalizeTripPlanResponse(savedResult) : mockTripPlan,
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
}

export function resetTripSession() {
  tripState.request = createDefaultPlanRequest()
  tripState.result = mockTripPlan
  tripState.hasResult = false
  tripState.loading = false
  tripState.error = ''
  window.sessionStorage.removeItem('travel-agent-request')
  window.sessionStorage.removeItem('travel-agent-result')
}
