package com.enterprise.smarttask.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AIRequestDTO {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    private String provider = "claude";  // "claude" or "bedrock"
}
