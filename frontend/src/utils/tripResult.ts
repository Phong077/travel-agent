import { mockTripPlan } from '../mock'
import type { BudgetAnalysis, ItineraryDay, KnowledgeReference, TripPlanResponse } from '../types'

function getStringValue(value: unknown, fallback: string) {
  return typeof value === 'string' && value.trim() ? value : fallback
}

function getNumberValue(value: unknown, fallback: number, minValue = 0) {
  return typeof value === 'number' && Number.isFinite(value) && value >= minValue ? value : fallback
}

function normalizeDay(value: Partial<ItineraryDay>, index: number): ItineraryDay {
  const fallback = mockTripPlan.days[index] ?? mockTripPlan.days[0]
  const dayNumber = getNumberValue(value.day, index + 1, 1)

  return {
    day: dayNumber,
    theme: getStringValue(value.theme, fallback.theme),
    morning: getStringValue(value.morning, fallback.morning),
    afternoon: getStringValue(value.afternoon, fallback.afternoon),
    evening: getStringValue(value.evening, fallback.evening),
    transportTip: getStringValue(value.transportTip, fallback.transportTip),
  }
}

function normalizeReference(value: Partial<KnowledgeReference>, index: number): KnowledgeReference {
  const fallback = mockTripPlan.references[index] ?? mockTripPlan.references[0]

  return {
    title: getStringValue(value.title, fallback.title),
    source: getStringValue(value.source, fallback.source),
    snippet: getStringValue(value.snippet, fallback.snippet),
    score: getNumberValue(value.score, fallback.score),
  }
}

function normalizeBudgetAnalysis(value: Partial<BudgetAnalysis> | undefined): BudgetAnalysis | undefined {
  if (!value) {
    return undefined
  }

  const fallback = mockTripPlan.budgetAnalysis

  return {
    perPersonBudget: getNumberValue(value.perPersonBudget, fallback?.perPersonBudget ?? 0),
    perPersonDailyBudget: getNumberValue(value.perPersonDailyBudget, fallback?.perPersonDailyBudget ?? 0),
    level: getStringValue(value.level, fallback?.level ?? '适中'),
    suggestion: getStringValue(value.suggestion, fallback?.suggestion ?? '暂无预算建议。'),
  }
}

export function normalizeTripPlanResponse(value: Partial<TripPlanResponse> | TripPlanResponse): TripPlanResponse {
  const rawDays = Array.isArray(value.days) ? value.days : mockTripPlan.days
  const rawReferences = Array.isArray(value.references) ? value.references : mockTripPlan.references

  return {
    destination: getStringValue(value.destination, mockTripPlan.destination),
    totalDays: getNumberValue(value.totalDays, rawDays.length || mockTripPlan.totalDays, 1),
    summary: getStringValue(value.summary, mockTripPlan.summary),
    days: rawDays.map((day, index) => normalizeDay(day, index)),
    references: rawReferences.map((reference, index) => normalizeReference(reference, index)),
    budgetAnalysis: normalizeBudgetAnalysis(value.budgetAnalysis),
  }
}
