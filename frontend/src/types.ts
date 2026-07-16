export interface PlanTripRequest {
  departureCity: string
  destination: string
  days: number
  travelers: number
  budget: number
  preferences: string[]
  avoid: string[]
}

export interface ItineraryDay {
  day: number
  theme: string
  morning: string
  afternoon: string
  evening: string
  transportTip: string
}

export interface KnowledgeReference {
  title: string
  source: string
  snippet: string
  score: number
}

export interface BudgetAnalysis {
  perPersonBudget: number
  perPersonDailyBudget: number
  level: string
  suggestion: string
}

export interface TripPlanResponse {
  destination: string
  totalDays: number
  summary: string
  days: ItineraryDay[]
  references: KnowledgeReference[]
  budgetAnalysis?: BudgetAnalysis
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface KnowledgeSearchResult {
  title: string
  source: string
  content: string
  score: number
}
