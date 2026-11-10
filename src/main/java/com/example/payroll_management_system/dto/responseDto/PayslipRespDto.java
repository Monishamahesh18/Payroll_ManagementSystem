package com.example.payroll_management_system.dto.responseDto;

import com.example.payroll_management_system.enums.PayStatus;

import java.time.LocalDate;

public record PayslipRespDto(
        Long payslipId,
        Long runId,
        LocalDate periodStart,
        LocalDate periodEnd,
        Long employeeId,
        String employeeName,
        Double gross,
        Double deduction,
        Double net,
        PayStatus payStatus,
        LocalDate paidOn
) {
}
