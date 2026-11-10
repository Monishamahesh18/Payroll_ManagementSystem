package com.example.payroll_management_system.dto.responseDto;

import com.example.payroll_management_system.enums.LeaveStatus;

import java.time.LocalDate;

public record LeaveRespDto(
        Long leaveId,
        LocalDate fromDate,
        LocalDate toDate,
        String type,
        LeaveStatus status,
        Long employeeId,
        String employeeName,
        String reviewedByName
) {
}
