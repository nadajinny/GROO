package com.groo.dto;

import com.groo.domain.task.TaskActivity;
import java.time.LocalDateTime;

public record TaskActivityResponse(
        Long id,
        Long userId,
        String userEmail,
        String userName,
        String message,
        LocalDateTime createdAt) {

    public static TaskActivityResponse from(TaskActivity activity) {
        return new TaskActivityResponse(
                activity.getId(),
                activity.getUser().getId(),
                activity.getUser().getEmail(),
                activity.getUser().getDisplayName(),
                activity.getMessage(),
                activity.getCreatedAt());
    }
}
