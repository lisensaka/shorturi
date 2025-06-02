package com.lhind.shorturi.entity.dto.request;

import jakarta.validation.constraints.NotNull;

public record AuthRequestDto(
        @NotNull(message = "Email cannot be null!") String username,
        @NotNull(message = "Password cannot be null!") String password) {
}
