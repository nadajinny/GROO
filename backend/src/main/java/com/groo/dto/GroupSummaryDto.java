package com.groo.dto;

import com.groo.domain.group.Group;
import com.groo.domain.group.GroupRole;
import com.groo.domain.group.GroupStatus;
import java.time.LocalDateTime;

public record GroupSummaryDto(
        Long id,
        String name,
        String description,
        GroupStatus status,
        GroupRole myRole,
        long memberCount,
        LocalDateTime createdAt) {

    public static GroupSummaryDto of(Group group, GroupRole role, long memberCount) {
        return new GroupSummaryDto(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getStatus(),
                role,
                memberCount,
                group.getCreatedAt());
    }
}
