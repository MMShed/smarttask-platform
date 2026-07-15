package com.enterprise.smarttask.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIResponse {
    private String provider;   // "claude" | "bedrock"
    private String suggestion;
    private String priority;
    private String category;
    private double confidence;
}
