import type { GenerationMode } from '../store/tripStore'

const MODE_LABELS: Record<GenerationMode, string> = {
  stable: '稳定服务编排',
  agent: 'Agent Tool Calling',
  'react-agent': 'ReactAgent',
  'multi-agent': '多 Agent 协同',
}

const MODE_DESCRIPTIONS: Record<GenerationMode, string> = {
  stable: '后端按固定顺序编排知识库、预算、天气和大模型生成流程。',
  agent: '大模型通过 Tool Calling 调用预算、天气和知识库能力后生成行程。',
  'react-agent': 'ReactAgent 通过 ReAct 循环自动选择并调用工具，再生成结构化行程。',
  'multi-agent': '多个角色协同完成知识检索、预算分析、天气分析和行程生成。',
}

export function getGenerationModeLabel(mode: GenerationMode) {
  return MODE_LABELS[mode]
}

export function getGenerationModeDescription(mode: GenerationMode) {
  return MODE_DESCRIPTIONS[mode]
}
