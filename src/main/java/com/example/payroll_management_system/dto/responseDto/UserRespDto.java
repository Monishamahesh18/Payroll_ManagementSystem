package com.example.payroll_management_system.dto.responseDto;

import com.example.payroll_management_system.enums.Role;

public record UserRespDto(
        Long userId,
        String email,
        Role role,
        Boolean isActive
) {
}
