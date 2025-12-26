package com.groo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateGroupRequest(
        @NotBlank @Size(max = 40) String name,
        @Size(max = 200) String description,
        boolean archived) {
}
