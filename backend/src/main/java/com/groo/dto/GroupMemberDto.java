package com.groo.dto;

import com.groo.domain.group.GroupMembership;
import com.groo.domain.group.GroupRole;
import java.time.LocalDateTime;

public record GroupMemberDto(
        Long membershipId,
        Long userId,
        String email,
        String displayName,
        GroupRole role,
        LocalDateTime joinedAt) {

    public static GroupMemberDto from(GroupMembership membership) {
        return new GroupMemberDto(
                membership.getId(),
                membership.getUser().getId(),
                membership.getUser().getEmail(),
                membership.getUser().getDisplayName(),
                membership.getRole(),
                membership.getJoinedAt());
    }
}
