package com.groo.dto;

import com.groo.domain.project.Project;
import com.groo.domain.project.ProjectStatus;
import java.time.LocalDateTime;

public record ProjectResponse(
        Long id,
        Long groupId,
        String name,
        String description,
        ProjectStatus status,
        Long createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getGroup().getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getCreatedBy().getId(),
                project.getCreatedAt(),
                project.getUpdatedAt());
    }
}
