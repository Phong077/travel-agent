import { reactive } from 'vue'
import { defaultRequest, mockTripPlan } from '../mock'
import type { PlanTripRequest, TripPlanResponse } from '../types'

interface TripState {
  request: PlanTripRequest
  result: TripPlanResponse
  loading: boolean
  error: string
}

const savedResult = window.sessionStorage.getItem('travel-agent-result')

export const tripState = reactive<TripState>({
  request: { ...defaultRequest },
  result: savedResult ? (JSON.parse(savedResult) as TripPlanResponse) : mockTripPlan,
  loading: false,
  error: '',
})

export function setTripResult(result: TripPlanResponse) {
  tripState.result = result
  window.sessionStorage.setItem('travel-agent-result', JSON.stringify(result))
}
