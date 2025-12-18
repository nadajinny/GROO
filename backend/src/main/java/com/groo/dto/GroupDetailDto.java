package com.groo.dto;

import com.groo.domain.group.Group;
import com.groo.domain.group.GroupRole;
import com.groo.domain.group.GroupStatus;
import java.time.LocalDateTime;
import java.util.List;

public record GroupDetailDto(
        Long id,
        String name,
        String description,
        GroupStatus status,
        GroupRole myRole,
        long memberCount,
        String invitationCode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<GroupMemberDto> members) {

    public static GroupDetailDto of(Group group, GroupRole myRole, long memberCount, List<GroupMemberDto> members) {
        return new GroupDetailDto(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getStatus(),
                myRole,
                memberCount,
                group.getInvitationCode(),
                group.getCreatedAt(),
                group.getUpdatedAt(),
                members);
    }
}
