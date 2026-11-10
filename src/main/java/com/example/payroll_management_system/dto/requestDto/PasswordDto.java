package com.example.payroll_management_system.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// admin resets the password of a user
public record PasswordDto(
        @NotBlank(message = "Password is required")
        @NotNull(message = "Password is required")
        String password
) {
}
