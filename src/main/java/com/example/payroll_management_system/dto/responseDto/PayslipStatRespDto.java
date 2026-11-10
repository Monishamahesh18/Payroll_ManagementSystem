package com.example.payroll_management_system.dto.responseDto;

public record PayslipStatRespDto(
        Long runId,
        long payslips,
        long paidPayslips,
        double totalGross,
        double totalDeduction,
        double totalNet
) {
}
