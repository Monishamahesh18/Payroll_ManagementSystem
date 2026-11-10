package com.example.payroll_management_system.dto.responseDto;

import java.time.Instant;

public record AuditLogRespDto(
        Long auditId,
        String action,
        String entity,
        Long entityId,
        Instant createdAt,
        Long userId,
        String userEmail
) {
}
