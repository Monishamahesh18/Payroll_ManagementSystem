package com.example.payroll_management_system.dto.responseDto;

import com.example.payroll_management_system.enums.RunStatus;

import java.util.List;

// issues is empty when the run is clean and can be submitted for HR review
public record PayrollVerifyRespDto(
        Long runId,
        RunStatus status,
        long totalPayslips,
        long issueCount,
        List<String> issues
) {
}
