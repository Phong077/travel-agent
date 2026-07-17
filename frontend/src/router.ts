import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from './pages/DashboardView.vue'
import PlanFormView from './pages/PlanFormView.vue'
import LoadingView from './pages/LoadingView.vue'
import ResultOverviewView from './pages/ResultOverviewView.vue'
import DayDetailView from './pages/DayDetailView.vue'
import ReferencesView from './pages/ReferencesView.vue'
import KnowledgeDebugView from './pages/KnowledgeDebugView.vue'
import HistoryView from './pages/HistoryView.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: DashboardView },
    { path: '/plan', component: PlanFormView },
    { path: '/loading', component: LoadingView },
    { path: '/result', component: ResultOverviewView },
    { path: '/days', component: DayDetailView },
    { path: '/references', component: ReferencesView },
    { path: '/knowledge', component: KnowledgeDebugView },
    { path: '/history', component: HistoryView },
  ],
})
