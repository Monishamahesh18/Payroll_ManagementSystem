package com.example.payroll_management_system.dto.responseDto;

// availableDays = yearlyQuota - approvedDays - pendingDays (calendar days)
public record LeaveBalanceRespDto(
        Long employeeId,
        String employeeName,
        int year,
        int yearlyQuota,
        long approvedDays,
        long pendingDays,
        long availableDays
) {
}
