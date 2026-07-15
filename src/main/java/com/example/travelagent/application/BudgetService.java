package com.example.travelagent.application;

import com.example.travelagent.domain.BudgetAnalysis;
import com.example.travelagent.domain.PlanTripRequest;
import org.springframework.stereotype.Service;

@Service
public class BudgetService {

    public BudgetAnalysis analyze(PlanTripRequest request) {
        int travelers = Math.max(request.travelers(), 1);
        int days = Math.max(request.days(), 1);

        int perPersonBudget = request.budget() / travelers;
        int perPersonDailyBudget = perPersonBudget / days;

        String level = getBudgetLevel(perPersonDailyBudget);
        String suggestion = getSuggestion(level);

        return new BudgetAnalysis(
                perPersonBudget,
                perPersonDailyBudget,
                level,
                suggestion
        );
    }

    private String getBudgetLevel(int perPersonDailyBudget) {
        if (perPersonDailyBudget < 300) {
            return "偏紧";
        }
        if (perPersonDailyBudget <= 700) {
            return "适中";
        }
        return "充裕";
    }

    private String getSuggestion(String level) {
        return switch (level) {
            case "偏紧" -> "预算偏紧，建议减少长距离交通和频繁换酒店，优先选择成都及周边一日游，住宿和餐饮以性价比为主。";
            case "适中" -> "预算适中，可以兼顾成都城市体验、周边景点和部分特色美食，建议控制长距离路线数量。";
            case "充裕" -> "预算较充裕，可以安排更舒适的住宿、特色餐厅和更灵活的交通方式，但仍需避免行程过度紧凑。";
            default -> "请根据预算合理安排行程。";
        };
    }
}
