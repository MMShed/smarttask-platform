package com.enterprise.smarttask.service;

import com.enterprise.smarttask.model.AIResponse;
import com.enterprise.smarttask.model.Task;
import com.enterprise.smarttask.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Processes open tasks in the background using a dedicated thread pool.
 * Demonstrates multi-threading, Collections (ConcurrentHashMap), and CompletableFuture.
 */
@Service
public class TaskProcessorService {

    private static final Logger log = LoggerFactory.getLogger(TaskProcessorService.class);

    private final TaskRepository taskRepository;
    private final ClaudeAIService claudeAIService;

    // Cache AI suggestions to avoid redundant API calls within a run cycle
    private final Map<Long, String> suggestionCache = new ConcurrentHashMap<>();

    private final ExecutorService enrichmentPool = Executors.newFixedThreadPool(4);

    public TaskProcessorService(TaskRepository taskRepository, ClaudeAIService claudeAIService) {
        this.taskRepository = taskRepository;
        this.claudeAIService = claudeAIService;
    }

    /**
     * Every 5 minutes: fetch OPEN tasks without AI suggestions and enrich them in parallel.
     */
    @Scheduled(fixedDelayString = "${smarttask.processor.interval-ms:300000}")
    public void enrichOpenTasks() {
        List<Task> openTasks = taskRepository.findByStatus(Task.Status.OPEN);

        List<CompletableFuture<Void>> futures = openTasks.stream()
                .filter(t -> t.getAiSuggestion() == null && !suggestionCache.containsKey(t.getId()))
                .map(task -> CompletableFuture
                        .supplyAsync(() -> fetchSuggestion(task), enrichmentPool)
                        .thenAccept(suggestion -> persistSuggestion(task, suggestion)))
                .collect(java.util.stream.Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .join();

        log.info("Enrichment cycle complete. Processed {} tasks.", futures.size());
    }

    private AIResponse fetchSuggestion(Task task) {
        try {
            return claudeAIService.triageTask(task.getTitle(), task.getDescription()).get();
        } catch (Exception e) {
            log.warn("AI enrichment failed for task {}: {}", task.getId(), e.getMessage());
            return AIResponse.builder().suggestion("N/A").priority("MEDIUM").build();
        }
    }

    private void persistSuggestion(Task task, AIResponse response) {
        task.setAiSuggestion(response.getSuggestion());
        if (task.getPriority() == null) {
            task.setPriority(Task.Priority.valueOf(response.getPriority()));
        }
        task.setStatus(Task.Status.OPEN);
        taskRepository.update(task);
        suggestionCache.put(task.getId(), response.getSuggestion());
    }
}
