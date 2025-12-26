package com.groo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinGroupRequest(
        @NotBlank
        @Size(min = 8, max = 32)
        String invitationCode) {
}
