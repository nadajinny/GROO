package com.groo.dto;

import com.groo.domain.task.TaskComment;
import java.time.LocalDateTime;

public record TaskCommentResponse(
        Long id,
        Long authorId,
        String authorEmail,
        String authorName,
        String content,
        LocalDateTime createdAt) {

    public static TaskCommentResponse from(TaskComment comment) {
        return new TaskCommentResponse(
                comment.getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getEmail(),
                comment.getAuthor().getDisplayName(),
                comment.getContent(),
                comment.getCreatedAt());
    }
}
