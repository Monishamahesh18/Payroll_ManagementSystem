package com.example.payroll_management_system.dto.responseDto;

import com.example.payroll_management_system.enums.PayStatus;

import java.time.LocalDate;
import java.util.List;

// pay stub: payslip totals plus every earning and deduction line
public record PayslipDetailRespDto(
        Long payslipId,
        Long runId,
        LocalDate periodStart,
        LocalDate periodEnd,
        Long employeeId,
        String employeeName,
        String bankAcc,
        Double gross,
        Double deduction,
        Double net,
        PayStatus payStatus,
        LocalDate paidOn,
        List<PayslipItemRespDto> items
) {
}
