package com.example.travelagent.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.example.travelagent.agent.TravelAgentTools;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ReactAgentConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.agent.react", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ReactAgent travelReactAgent(ChatModel chatModel, TravelAgentTools travelAgentTools) {
        return ReactAgent.builder()
                .name("travel-planning-react-agent")
                .description("用于生成旅行计划的 ReAct 智能体")
                .model(chatModel)
                // 复用现有 @Tool 方法，让 ReactAgent 可以按需调用预算、天气和知识库检索工具。
                .methodTools(travelAgentTools)
                // 当前工具调用记录使用 ThreadLocal，关闭并行工具执行可以让调用链更容易追踪和展示。
                .parallelToolExecution(false)
                .wrapSyncToolsAsAsync(false)
                .toolExecutionTimeout(Duration.ofSeconds(30))
                .instruction("""
                        你是一个专业的中文旅行规划 ReAct Agent。

                        你需要根据用户给出的出发地、目的地、天数、人数、预算、偏好和避坑项完成旅行计划。
                        在生成最终计划前，必须调用可用工具完成预算分析、天气分析和知识库检索。

                        工具使用原则：
                        1. 预算会影响住宿、交通、餐饮和景点选择。
                        2. 天气会影响户外活动、交通时间和备选安排。
                        3. 知识库引用用于减少自由发挥，并让行程更贴合目的地。

                        最终回答必须使用简体中文。
                        """)
                .build();
    }
}
