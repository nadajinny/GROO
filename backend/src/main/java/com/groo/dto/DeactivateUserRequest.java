package com.groo.dto;

import jakarta.validation.constraints.NotBlank;

public record DeactivateUserRequest(@NotBlank String reason) {
}
