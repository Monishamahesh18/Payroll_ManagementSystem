package com.example.payroll_management_system.dto.requestDto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PayrollrunDto(
        @NotNull(message = "Period start is required")
        LocalDate periodStart,

        @NotNull(message = "Period end is required")
        LocalDate periodEnd
) {
}
