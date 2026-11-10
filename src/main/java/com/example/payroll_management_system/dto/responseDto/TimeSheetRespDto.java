package com.example.payroll_management_system.dto.responseDto;

import com.example.payroll_management_system.enums.TimesheetStatus;

import java.time.LocalDate;

public record TimeSheetRespDto(
        Long timesheetId,
        LocalDate workDate,
        Double hours,
        TimesheetStatus status,
        Long employeeId,
        String employeeName
) {
}
