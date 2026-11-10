package com.example.payroll_management_system.dto.responseDto;

import com.example.payroll_management_system.enums.Role;

public record DetailsDto(
        String username,
        Boolean isActive,
        Role role
) {
}
