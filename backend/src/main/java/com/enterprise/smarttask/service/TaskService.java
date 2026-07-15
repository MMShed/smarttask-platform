package com.enterprise.smarttask.service;

import com.enterprise.smarttask.dto.TaskDTO;
import com.enterprise.smarttask.model.AIResponse;
import com.enterprise.smarttask.model.Task;
import com.enterprise.smarttask.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ClaudeAIService claudeAIService;
    private final BedrockAIService bedrockAIService;

    public TaskService(TaskRepository taskRepository,
                       ClaudeAIService claudeAIService,
                       BedrockAIService bedrockAIService) {
        this.taskRepository = taskRepository;
        this.claudeAIService = claudeAIService;
        this.bedrockAIService = bedrockAIService;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
    }

    public Task createTask(TaskDTO dto, String aiProvider) {
        AIResponse aiResponse = runAITriage(dto.getTitle(), dto.getDescription(), aiProvider);

        Task task = Task.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .priority(dto.getPriority() != null
                        ? dto.getPriority()
                        : Task.Priority.valueOf(aiResponse.getPriority()))
                .assignee(dto.getAssignee())
                .aiSuggestion(aiResponse.getSuggestion())
                .build();

        return taskRepository.save(task);
    }

    public Task updateTask(Long id, TaskDTO dto) {
        Task existing = getTaskById(id);
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        if (dto.getPriority() != null) existing.setPriority(dto.getPriority());
        if (dto.getAssignee() != null) existing.setAssignee(dto.getAssignee());
        taskRepository.update(existing);
        return existing;
    }

    public void resolveTask(Long id) {
        Task task = getTaskById(id);
        task.setStatus(Task.Status.RESOLVED);
        taskRepository.update(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public List<Map<String, Object>> getTaskSummary() {
        return taskRepository.getTaskSummary();
    }

    private AIResponse runAITriage(String title, String description, String provider) {
        try {
            CompletableFuture<AIResponse> future = "bedrock".equalsIgnoreCase(provider)
                    ? bedrockAIService.triageTask(title, description)
                    : claudeAIService.triageTask(title, description);
            return future.get();
        } catch (Exception e) {
            return AIResponse.builder()
                    .provider(provider)
                    .priority("MEDIUM")
                    .suggestion("AI triage unavailable.")
                    .confidence(0.0)
                    .build();
        }
    }
}
