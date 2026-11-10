package com.example.payroll_management_system.dto.responseDto;

import com.example.payroll_management_system.enums.EmployeeStatus;
import com.example.payroll_management_system.enums.Role;

import java.time.LocalDate;

public record EmployeeRespDto(
        Long employeeId,
        String empCode,
        String fullName,
        String department,
        LocalDate joinDate,
        EmployeeStatus status,
        Double basic,
        Double hra,
        Double allowance,
        String bankAcc,
        String phone,
        String address,
        Double taxWithholding,
        String email,
        Role role,
        Long managerId,
        String managerName
) {
}
