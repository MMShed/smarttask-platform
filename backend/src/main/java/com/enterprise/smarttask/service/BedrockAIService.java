package com.enterprise.smarttask.service;

import com.enterprise.smarttask.model.AIResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.concurrent.CompletableFuture;

@Service
public class BedrockAIService {

    // Claude 3 Sonnet hosted on Bedrock
    private static final String MODEL_ID = "anthropic.claude-3-sonnet-20240229-v1:0";

    private final BedrockRuntimeClient bedrockClient;
    private final ObjectMapper objectMapper;

    public BedrockAIService(BedrockRuntimeClient bedrockClient, ObjectMapper objectMapper) {
        this.bedrockClient = bedrockClient;
        this.objectMapper = objectMapper;
    }

    @Async("aiCallExecutor")
    public CompletableFuture<AIResponse> triageTask(String title, String description) {
        try {
            String prompt = buildPrompt(title, description);

            // Anthropic Claude on Bedrock uses the Messages API payload
            String requestBody = objectMapper.writeValueAsString(objectMapper.createObjectNode()
                    .put("anthropic_version", "bedrock-2023-05-31")
                    .put("max_tokens", 512)
                    .set("messages", objectMapper.createArrayNode()
                            .add(objectMapper.createObjectNode()
                                    .put("role", "user")
                                    .put("content", prompt))));

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(MODEL_ID)
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(requestBody))
                    .build();

            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String responseBody = response.body().asUtf8String();

            return CompletableFuture.completedFuture(parseBedrockResponse(responseBody));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(AIResponse.builder()
                    .provider("bedrock")
                    .suggestion("Bedrock triage unavailable: " + e.getMessage())
                    .priority("MEDIUM")
                    .confidence(0.0)
                    .build());
        }
    }

    private String buildPrompt(String title, String description) {
        return String.format(
            "Analyze this IT task and respond ONLY in JSON: " +
            "{\"priority\":\"HIGH\",\"category\":\"...\",\"suggestion\":\"...\",\"confidence\":0.9}\n\n" +
            "Title: %s\nDescription: %s", title, description);
    }

    private AIResponse parseBedrockResponse(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        String text = root.path("content").get(0).path("text").asText();
        text = text.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();

        JsonNode parsed = objectMapper.readTree(text);
        return AIResponse.builder()
                .provider("bedrock")
                .priority(parsed.path("priority").asText("MEDIUM"))
                .category(parsed.path("category").asText("General"))
                .suggestion(parsed.path("suggestion").asText())
                .confidence(parsed.path("confidence").asDouble(0.8))
                .build();
    }
}
