package com.example.payroll_management_system.dto.requestDto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TimesheetDto(
        @NotNull(message = "Work date is required")
        LocalDate workDate,

        @NotNull(message = "Hours is required")
        Double hours
) {
}
