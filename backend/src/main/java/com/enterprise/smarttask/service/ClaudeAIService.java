package com.enterprise.smarttask.service;

import com.enterprise.smarttask.model.AIResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Service
public class ClaudeAIService {

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-sonnet-4-6";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${anthropic.api.key}")
    private String apiKey;

    public ClaudeAIService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Async("aiCallExecutor")
    public CompletableFuture<AIResponse> triageTask(String title, String description) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", "2023-06-01");

            String prompt = buildTriagePrompt(title, description);

            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", MODEL);
            body.put("max_tokens", 512);

            ArrayNode messages = body.putArray("messages");
            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);

            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(CLAUDE_API_URL, request, String.class);

            return CompletableFuture.completedFuture(parseClaudeResponse(response.getBody()));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(AIResponse.builder()
                    .provider("claude")
                    .suggestion("AI triage unavailable: " + e.getMessage())
                    .priority("MEDIUM")
                    .confidence(0.0)
                    .build());
        }
    }

    private String buildTriagePrompt(String title, String description) {
        return String.format(
            "You are an enterprise IT incident triage assistant. Analyze the following task and respond " +
            "in JSON with fields: priority (LOW/MEDIUM/HIGH/CRITICAL), category (string), " +
            "suggestion (1-2 sentence resolution guidance), confidence (0.0-1.0).\n\n" +
            "Title: %s\nDescription: %s\n\nRespond with only valid JSON.", title, description);
    }

    private AIResponse parseClaudeResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        String text = root.path("content").get(0).path("text").asText();

        // Strip markdown code fences if present
        text = text.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();

        JsonNode parsed = objectMapper.readTree(text);
        return AIResponse.builder()
                .provider("claude")
                .priority(parsed.path("priority").asText("MEDIUM"))
                .category(parsed.path("category").asText("General"))
                .suggestion(parsed.path("suggestion").asText())
                .confidence(parsed.path("confidence").asDouble(0.8))
                .build();
    }
}
