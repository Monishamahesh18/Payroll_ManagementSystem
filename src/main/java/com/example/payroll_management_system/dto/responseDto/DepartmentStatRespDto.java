package com.example.payroll_management_system.dto.responseDto;

// Department | Number of Employees
public record DepartmentStatRespDto(
        String department,
        long numberOfEmployees
) {
}
