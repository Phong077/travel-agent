package com.example.travelagent.application.multiagent;

import com.example.travelagent.domain.BudgetAnalysis;
import com.example.travelagent.domain.GenerationMetadata;
import com.example.travelagent.domain.ItineraryDay;
import com.example.travelagent.domain.KnowledgeReference;
import com.example.travelagent.domain.PlanTripRequest;
import com.example.travelagent.domain.ToolCallRecord;
import com.example.travelagent.domain.TripPlanResponse;
import com.example.travelagent.domain.WeatherInfo;
import com.example.travelagent.knowledge.KnowledgeSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 多 Agent 旅行规划总协调者。
 *
 * <p>Coordinator 不直接处理具体业务细节，而是按顺序调度多个子 Agent：
 * 知识库检索、预算分析、天气分析、行程生成。这样每个 Agent 职责更单一，
 * 面试时也能清楚讲出“谁负责什么、数据怎么流动”。</p>
 */
@Service
public class MultiAgentTravelPlanningService {

    private static final Logger log = LoggerFactory.getLogger(MultiAgentTravelPlanningService.class);

    private final KnowledgeRetrievalAgent knowledgeRetrievalAgent;
    private final BudgetAnalysisAgent budgetAnalysisAgent;
    private final WeatherAnalysisAgent weatherAnalysisAgent;
    private final ItineraryGenerationAgent itineraryGenerationAgent;

    public MultiAgentTravelPlanningService(
            KnowledgeRetrievalAgent knowledgeRetrievalAgent,
            BudgetAnalysisAgent budgetAnalysisAgent,
            WeatherAnalysisAgent weatherAnalysisAgent,
            ItineraryGenerationAgent itineraryGenerationAgent
    ) {
        this.knowledgeRetrievalAgent = knowledgeRetrievalAgent;
        this.budgetAnalysisAgent = budgetAnalysisAgent;
        this.weatherAnalysisAgent = weatherAnalysisAgent;
        this.itineraryGenerationAgent = itineraryGenerationAgent;
    }

    public TripPlanResponse plan(PlanTripRequest request) {
        MultiAgentPlanningContext context = new MultiAgentPlanningContext(request);

        log.info(
                "Multi-agent planning started: departureCity={}, destination={}, days={}, travelers={}, budget={}",
                request.departureCity(),
                request.destination(),
                request.days(),
                request.travelers(),
                request.budget()
        );

        try {
            knowledgeRetrievalAgent.execute(context);
            budgetAnalysisAgent.execute(context);
            weatherAnalysisAgent.execute(context);
            itineraryGenerationAgent.execute(context);

            return buildValidatedResponse(context);
        } catch (RuntimeException exception) {
            log.warn("Multi-agent planning failed, using multi-agent fallback. destination={}",
                    request.destination(), exception);
            return buildFallbackResponse(context, exception.getMessage());
        }
    }

    private TripPlanResponse buildValidatedResponse(MultiAgentPlanningContext context) {
        TripPlanResponse generatedPlan = context.generatedPlan();

        return new TripPlanResponse(
                generatedPlan.destination(),
                generatedPlan.totalDays(),
                generatedPlan.summary(),
                generatedPlan.days(),
                toKnowledgeReferences(context.references()),
                context.budgetAnalysis(),
                context.weatherInfo(),
                context.toolCalls(),
                new GenerationMetadata("multi-agent", context.generationAttempts(), true)
        );
    }

    private TripPlanResponse buildFallbackResponse(MultiAgentPlanningContext context, String failureReason) {
        PlanTripRequest request = context.request();
        BudgetAnalysis budgetAnalysis = context.budgetAnalysis();
        WeatherInfo weatherInfo = context.weatherInfo();
        List<ToolCallRecord> toolCalls = new ArrayList<>(context.toolCalls());

        toolCalls.add(new ToolCallRecord(
                "multi-agent.fallback",
                "多 Agent 兜底策略",
                "已启用",
                "多 Agent 生成失败，原因：" + (failureReason == null ? "未知" : failureReason)
        ));

        return new TripPlanResponse(
                request.destination(),
                request.days(),
                buildFallbackSummary(request, context.references(), budgetAnalysis, weatherInfo),
                buildFallbackDays(request, context.references(), weatherInfo),
                toKnowledgeReferences(context.references()),
                budgetAnalysis,
                weatherInfo,
                toolCalls,
                new GenerationMetadata("multi-agent-fallback", context.generationAttempts(), false)
        );
    }

    private String buildFallbackSummary(
            PlanTripRequest request,
            List<KnowledgeSearchResult> references,
            BudgetAnalysis budgetAnalysis,
            WeatherInfo weatherInfo
    ) {
        String budgetText = budgetAnalysis == null ? "预算暂未完成分析" : "预算等级为" + budgetAnalysis.level();
        String weatherText = weatherInfo == null ? "天气暂未完成分析" : "天气风险为" + weatherInfo.riskLevel();

        return "这是多 Agent 兜底生成的“" + request.destination() + "”旅行计划。"
                + "已参考 " + references.size() + " 条知识库资料，"
                + budgetText + "，"
                + weatherText + "。";
    }

    private List<ItineraryDay> buildFallbackDays(
            PlanTripRequest request,
            List<KnowledgeSearchResult> references,
            WeatherInfo weatherInfo
    ) {
        List<ItineraryDay> days = new ArrayList<>();
        List<String> themes = List.of(
                "抵达与城市初印象",
                "核心景点与本地体验",
                "自然风光与轻松探索",
                "文化街区与深度漫游",
                "返程前的弹性安排"
        );

        for (int index = 0; index < request.days(); index++) {
            int day = index + 1;
            String referenceTitle = references.isEmpty()
                    ? request.destination() + "核心区域"
                    : references.get(Math.min(index, references.size() - 1)).title();
            String weatherTip = weatherInfo == null || weatherInfo.dailyTips() == null || weatherInfo.dailyTips().isEmpty()
                    ? "出发前查看实时天气。"
                    : weatherInfo.dailyTips().get(Math.min(index, weatherInfo.dailyTips().size() - 1));

            days.add(new ItineraryDay(
                    day,
                    request.destination() + (index < themes.size() ? themes.get(index) : "第 " + day + " 天弹性探索"),
                    day == 1
                            ? "从" + request.departureCity() + "出发前往" + request.destination() + "，抵达后优先入住交通便利区域，保留休整时间。"
                            : "上午围绕“" + referenceTitle + "”安排轻量游览，避免过早出发和过度赶路。",
                    "下午结合用户偏好安排体验，优先选择交通顺路、节奏稳定的地点。天气提醒：" + weatherTip,
                    "晚上安排本地餐饮、城市漫步或轻松休息，尽量避开用户填写的避坑项。",
                    "多 Agent 兜底交通建议：优先选择公共交通、城际铁路和短距离步行，跨城或远距离景点需要预留交通缓冲。"
            ));
        }

        return days;
    }

    private List<KnowledgeReference> toKnowledgeReferences(List<KnowledgeSearchResult> references) {
        return references.stream()
                .map(reference -> new KnowledgeReference(
                        reference.title(),
                        reference.source(),
                        toSnippet(reference.content()),
                        reference.score()
                ))
                .toList();
    }

    private String toSnippet(String content) {
        if (content == null || content.length() <= 80) {
            return content;
        }
        return content.substring(0, 80) + "...";
    }
}
