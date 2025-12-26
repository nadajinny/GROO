package com.groo.dto;

import com.groo.domain.task.TaskPriority;
import com.groo.domain.task.TaskStatus;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record UpdateTaskRequest(
        TaskStatus status,
        TaskPriority priority,
        @Size(max = 120) String assigneeId,
        Boolean updateAssignee,
        Instant dueDate,
        Boolean updateDueDate) {
}
