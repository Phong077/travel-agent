package com.example.travelagent.api;

import com.example.travelagent.domain.HealthStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final String applicationName;

    public HealthController(@Value("${spring.application.name:travel-agent}") String applicationName) {
        this.applicationName = applicationName;
    }

    @GetMapping
    public ApiResponse<HealthStatus> health() {
        return ApiResponse.success(new HealthStatus(
                applicationName,
                "UP",
                OffsetDateTime.now().toString()
        ));
    }
}
