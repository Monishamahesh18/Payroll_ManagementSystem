package com.example.payroll_management_system.dto.responseDto;

import com.example.payroll_management_system.enums.ComponentType;

public record PaycomponentRespDto(
        Long id,
        String name,
        ComponentType type,
        Double value,
        boolean isStatutory,
        boolean isActive
) {
}
