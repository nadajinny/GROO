package com.groo.dto;

import com.groo.domain.task.TaskPriority;
import com.groo.domain.task.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateTaskRequest(
        @NotNull Long projectId,
        @NotBlank @Size(max = 120) String title,
        @Size(max = 1000) String description,
        @Size(max = 120) String assigneeId,
        Instant dueDate,
        TaskStatus status,
        TaskPriority priority) {
}
