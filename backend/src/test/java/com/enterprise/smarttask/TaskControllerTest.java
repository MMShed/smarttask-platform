package com.enterprise.smarttask;

import com.enterprise.smarttask.controller.TaskController;
import com.enterprise.smarttask.dto.TaskDTO;
import com.enterprise.smarttask.model.Task;
import com.enterprise.smarttask.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private TaskService taskService;

    @Test
    void getAllTasks_returns200WithList() throws Exception {
        Task t = Task.builder().id(1L).title("DB issue").status(Task.Status.OPEN)
                .priority(Task.Priority.HIGH).createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()).build();
        when(taskService.getAllTasks()).thenReturn(List.of(t));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("DB issue"));
    }

    @Test
    void createTask_validPayload_returns201() throws Exception {
        TaskDTO dto = new TaskDTO();
        dto.setTitle("Network outage");
        dto.setDescription("Core switch unresponsive in DC1");

        Task created = Task.builder().id(10L).title(dto.getTitle())
                .status(Task.Status.OPEN).priority(Task.Priority.CRITICAL)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        when(taskService.createTask(any(TaskDTO.class), anyString())).thenReturn(created);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void createTask_missingTitle_returns400() throws Exception {
        TaskDTO dto = new TaskDTO();
        dto.setDescription("Some description");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resolveTask_returns204() throws Exception {
        mockMvc.perform(patch("/api/tasks/1/resolve"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTask_returns204() throws Exception {
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
    }
}
