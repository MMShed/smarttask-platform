package com.enterprise.smarttask.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private Long id;
    private String title;
    private String description;
    private Priority priority;
    private Status status;
    private String assignee;
    private String aiSuggestion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }
    public enum Status   { OPEN, IN_PROGRESS, RESOLVED, CLOSED }
}
