package com.enterprise.smarttask.dto;

import com.enterprise.smarttask.model.Task;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class TaskDTO {
    @NotBlank(message = "Title is required")
    @Size(max = 255)
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private Task.Priority priority;
    private String assignee;
}
