package com.example.payroll_management_system.dto.requestDto;

import com.example.payroll_management_system.enums.ComponentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaycomponentDto(
        @NotBlank(message = "Name is required")
        @NotNull(message = "Name is required")
        String name,

        @NotNull(message = "Type is required")
        ComponentType type,

        @NotNull(message = "Value is required")
        @Positive(message = "Value must be greater than 0")
        Double value,

        boolean isStatutory
) {
}
