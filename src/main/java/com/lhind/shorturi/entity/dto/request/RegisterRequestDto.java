package com.lhind.shorturi.entity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record RegisterRequestDto(
        @NotNull(message = "Email cannot be null!") @Email(message = "Email must be a well-formed email address: test@example.com") String username,
        @NotNull(message = "Password cannot be null!") String password) {

    public RegisterRequestDto(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
