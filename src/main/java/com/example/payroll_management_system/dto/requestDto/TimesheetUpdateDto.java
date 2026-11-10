package com.example.payroll_management_system.dto.requestDto;

import jakarta.validation.constraints.NotNull;

public record TimesheetUpdateDto(
        @NotNull(message = "Hours is required")
        Double hours
) {
}
