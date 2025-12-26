package com.groo.dto;

import jakarta.validation.constraints.NotBlank;

public record SocialLoginRequest(@NotBlank String idToken) {
}
