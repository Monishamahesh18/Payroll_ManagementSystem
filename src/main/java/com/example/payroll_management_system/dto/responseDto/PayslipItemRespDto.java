package com.example.payroll_management_system.dto.responseDto;

import com.example.payroll_management_system.enums.ComponentType;

public record PayslipItemRespDto(
        String name,
        ComponentType type,
        Double amount,
        boolean isStatutory
) {
}
