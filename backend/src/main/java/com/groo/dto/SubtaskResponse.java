package com.groo.dto;

import com.groo.domain.task.TaskSubtask;
import java.time.LocalDateTime;

public record SubtaskResponse(Long id, String title, boolean done, LocalDateTime createdAt) {

    public static SubtaskResponse from(TaskSubtask subtask) {
        return new SubtaskResponse(subtask.getId(), subtask.getTitle(), subtask.isDone(), subtask.getCreatedAt());
    }
}
