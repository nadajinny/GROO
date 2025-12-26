package com.groo.dto;

import com.groo.domain.user.Role;
import com.groo.domain.user.User;
import com.groo.domain.user.UserStatus;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String displayName,
        Role role,
        UserStatus status,
        LocalDateTime createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt());
    }
}
