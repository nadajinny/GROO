package com.groo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotNull Long groupId,
        @NotBlank @Size(max = 80) String name,
        @Size(max = 500) String description) {
}
