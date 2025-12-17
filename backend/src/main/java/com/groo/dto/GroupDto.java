package com.groo.dto;

import com.groo.domain.group.Group;
import java.time.LocalDateTime;

public record GroupDto(Long id, String name, String description, LocalDateTime createdAt) {

    public static GroupDto from(Group group) {
        return new GroupDto(group.getId(), group.getName(), group.getDescription(), group.getCreatedAt());
    }
}
