package com.example.payroll_management_system.dto.responseDto;

import com.example.payroll_management_system.enums.ComponentType;

// one line for every statutory item of the run (PF, tax withholding etc.)
public record ComplianceRespDto(
        String itemName,
        ComponentType componentType,
        long employeeCount,
        double totalAmount
) {
}
