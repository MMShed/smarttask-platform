package com.enterprise.smarttask;

import com.enterprise.smarttask.dto.TaskDTO;
import com.enterprise.smarttask.model.AIResponse;
import com.enterprise.smarttask.model.Task;
import com.enterprise.smarttask.repository.TaskRepository;
import com.enterprise.smarttask.service.BedrockAIService;
import com.enterprise.smarttask.service.ClaudeAIService;
import com.enterprise.smarttask.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private ClaudeAIService claudeAIService;
    @Mock private BedrockAIService bedrockAIService;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, claudeAIService, bedrockAIService);
    }

    @Test
    void createTask_withClaudeProvider_persistsAISuggestion() throws Exception {
        AIResponse aiResponse = AIResponse.builder()
                .provider("claude")
                .priority("HIGH")
                .suggestion("Restart the JBoss server and check heap dumps.")
                .confidence(0.92)
                .build();

        when(claudeAIService.triageTask(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(aiResponse));

        Task saved = Task.builder().id(1L).title("Server OOM").description("OutOfMemoryError in prod")
                .priority(Task.Priority.HIGH).status(Task.Status.OPEN).build();
        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        TaskDTO dto = new TaskDTO();
        dto.setTitle("Server OOM");
        dto.setDescription("OutOfMemoryError in prod");

        Task result = taskService.createTask(dto, "claude");

        assertThat(result.getId()).isEqualTo(1L);
        verify(claudeAIService).triageTask("Server OOM", "OutOfMemoryError in prod");
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_withBedrockProvider_callsBedrockService() throws Exception {
        AIResponse aiResponse = AIResponse.builder()
                .provider("bedrock").priority("MEDIUM").suggestion("Check logs.").confidence(0.7).build();

        when(bedrockAIService.triageTask(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(aiResponse));
        when(taskRepository.save(any())).thenReturn(Task.builder().id(2L).build());

        TaskDTO dto = new TaskDTO();
        dto.setTitle("Slow query");
        dto.setDescription("Report query takes 30s");

        taskService.createTask(dto, "bedrock");

        verify(bedrockAIService).triageTask(anyString(), anyString());
        verifyNoInteractions(claudeAIService);
    }

    @Test
    void getTaskById_notFound_throwsException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.getTaskById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void resolveTask_setsStatusToResolved() {
        Task task = Task.builder().id(5L).status(Task.Status.OPEN)
                .priority(Task.Priority.MEDIUM).build();
        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));

        taskService.resolveTask(5L);

        assertThat(task.getStatus()).isEqualTo(Task.Status.RESOLVED);
        verify(taskRepository).update(task);
    }

    @Test
    void getAllTasks_returnsList() {
        List<Task> tasks = List.of(
                Task.builder().id(1L).title("T1").build(),
                Task.builder().id(2L).title("T2").build());
        when(taskRepository.findAll()).thenReturn(tasks);

        List<Task> result = taskService.getAllTasks();

        assertThat(result).hasSize(2);
    }
}
