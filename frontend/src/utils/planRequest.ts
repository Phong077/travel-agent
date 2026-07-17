import { defaultRequest } from '../mock'
import type { PlanTripRequest } from '../types'

function getStringValue(value: unknown, fallback: string) {
  return typeof value === 'string' && value.trim() ? value : fallback
}

function getNumberValue(value: unknown, fallback: number, minValue: number) {
  return typeof value === 'number' && Number.isFinite(value) && value >= minValue ? value : fallback
}

function getStringArrayValue(value: unknown, fallback: string[]) {
  if (!Array.isArray(value)) {
    return [...fallback]
  }

  const values = value.filter((item): item is string => typeof item === 'string' && item.trim().length > 0)
  return values.length > 0 ? values : [...fallback]
}

export function clonePlanRequest(request: Partial<PlanTripRequest> | PlanTripRequest): PlanTripRequest {
  return {
    departureCity: getStringValue(request.departureCity, defaultRequest.departureCity),
    destination: getStringValue(request.destination, defaultRequest.destination),
    days: getNumberValue(request.days, defaultRequest.days, 1),
    travelers: getNumberValue(request.travelers, defaultRequest.travelers, 1),
    budget: getNumberValue(request.budget, defaultRequest.budget, 500),
    preferences: getStringArrayValue(request.preferences, defaultRequest.preferences),
    avoid: getStringArrayValue(request.avoid, defaultRequest.avoid),
  }
}

export function createDefaultPlanRequest(): PlanTripRequest {
  return clonePlanRequest(defaultRequest)
}
