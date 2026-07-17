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

export interface WeatherInfo {
  destination: string
  summary: string
  riskLevel: string
  suggestion: string
  dailyTips: string[]
}

export interface ToolCallRecord {
  name: string
  displayName: string
  status: string
  detail: string
}

export interface HealthStatus {
  application: string
  status: string
  timestamp: string
}

export interface GenerationMetadata {
  mode: string
  attempts: number
  validated: boolean
}

export interface TripPlanResponse {
  destination: string
  totalDays: number
  summary: string
  days: ItineraryDay[]
  references: KnowledgeReference[]
  budgetAnalysis?: BudgetAnalysis
  weatherInfo?: WeatherInfo
  toolCalls?: ToolCallRecord[]
  generationMetadata?: GenerationMetadata
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

export interface KnowledgeDebugResponse {
  request: PlanTripRequest
  destinationKey: string
  dedicatedKnowledgeBase: boolean
  vectorStoreEnabled: boolean
  retrievalMode: string
  query: string
  results: KnowledgeSearchResult[]
}
