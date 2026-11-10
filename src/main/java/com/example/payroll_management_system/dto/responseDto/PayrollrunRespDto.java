package com.example.payroll_management_system.dto.responseDto;

import com.example.payroll_management_system.enums.RunStatus;

import java.time.LocalDate;

public record PayrollrunRespDto(
        Long runId,
        LocalDate periodStart,
        LocalDate periodEnd,
        RunStatus status,
        String processedBy,
        String approvedBy,
        String remarks
) {
}
