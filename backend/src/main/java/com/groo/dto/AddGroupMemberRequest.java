package com.groo.dto;

import com.groo.domain.group.GroupRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AddGroupMemberRequest(
        @Email @NotBlank String email,
        GroupRole role) {
}
