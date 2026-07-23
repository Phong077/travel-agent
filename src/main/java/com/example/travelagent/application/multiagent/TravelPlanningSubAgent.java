package com.example.travelagent.application.multiagent;

/**
 * 旅行规划子 Agent 的统一接口。
 *
 * <p>每个子 Agent 都只做一件事：从上下文读取已有信息，
 * 再把自己的分析结果写回上下文。这样 Coordinator 就可以按顺序编排多个 Agent。</p>
 */
public interface TravelPlanningSubAgent {

    /**
     * 子 Agent 的名称，用于日志、调试和前端工具调用展示。
     */
    String name();

    /**
     * 执行当前子 Agent 的任务。
     */
    void execute(MultiAgentPlanningContext context);
}
