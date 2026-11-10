package com.example.payroll_management_system.dto.requestDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// used for admin and processor, both need only email and password
public record AdminDto(
        @NotBlank(message = "Email is required")
        @NotNull(message = "Email is required")
        @Email(message = "Email seems invalid")
        String email,

        @NotBlank(message = "Password is required")
        @NotNull(message = "Password is required")
        String password
) {
}
