package com.example.travelagent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiChatController {

    private final ChatClient chatClient;

    public AiChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/api/ai/chat")
    public String chat(@RequestParam(defaultValue = "Plan a relaxed 5-day trip in Sichuan.") String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
