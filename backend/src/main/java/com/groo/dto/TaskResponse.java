package com.groo.dto;

import com.groo.domain.task.Task;
import com.groo.domain.task.TaskPriority;
import com.groo.domain.task.TaskStatus;
import java.time.Instant;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        Long projectId,
        String title,
        String description,
        String assigneeId,
        TaskStatus status,
        TaskPriority priority,
        Instant dueDate,
        Long createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getProject().getId(),
                task.getTitle(),
                task.getDescription(),
                task.getAssigneeId(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getCreatedBy().getId(),
                task.getCreatedAt(),
                task.getUpdatedAt());
    }
}
