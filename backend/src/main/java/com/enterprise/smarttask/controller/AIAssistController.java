package com.enterprise.smarttask.controller;

import com.enterprise.smarttask.dto.AIRequestDTO;
import com.enterprise.smarttask.model.AIResponse;
import com.enterprise.smarttask.service.BedrockAIService;
import com.enterprise.smarttask.service.ClaudeAIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIAssistController {

    private final ClaudeAIService claudeAIService;
    private final BedrockAIService bedrockAIService;

    public AIAssistController(ClaudeAIService claudeAIService, BedrockAIService bedrockAIService) {
        this.claudeAIService = claudeAIService;
        this.bedrockAIService = bedrockAIService;
    }

    @PostMapping("/triage")
    public ResponseEntity<AIResponse> triage(@Valid @RequestBody AIRequestDTO dto) throws Exception {
        CompletableFuture<AIResponse> future = "bedrock".equalsIgnoreCase(dto.getProvider())
                ? bedrockAIService.triageTask(dto.getTitle(), dto.getDescription())
                : claudeAIService.triageTask(dto.getTitle(), dto.getDescription());
        return ResponseEntity.ok(future.get());
    }

    /** Calls both providers in parallel and returns both responses for comparison. */
    @PostMapping("/triage/compare")
    public ResponseEntity<List<AIResponse>> triageCompare(@Valid @RequestBody AIRequestDTO dto) throws Exception {
        CompletableFuture<AIResponse> claudeFuture = claudeAIService.triageTask(dto.getTitle(), dto.getDescription());
        CompletableFuture<AIResponse> bedrockFuture = bedrockAIService.triageTask(dto.getTitle(), dto.getDescription());
        CompletableFuture.allOf(claudeFuture, bedrockFuture).get();
        return ResponseEntity.ok(List.of(claudeFuture.get(), bedrockFuture.get()));
    }
}
