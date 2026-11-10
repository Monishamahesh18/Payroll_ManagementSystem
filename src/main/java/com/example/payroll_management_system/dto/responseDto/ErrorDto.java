package com.example.payroll_management_system.dto.responseDto;

import java.time.Instant;

public record ErrorDto(
        String message,
        String comments,
        Instant timestamp
) {
}
