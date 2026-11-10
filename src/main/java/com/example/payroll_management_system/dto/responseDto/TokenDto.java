package com.example.payroll_management_system.dto.responseDto;

import java.util.Date;

public record TokenDto(
        String token,
        Date expiryTime,
        String issueTime
) {
}
