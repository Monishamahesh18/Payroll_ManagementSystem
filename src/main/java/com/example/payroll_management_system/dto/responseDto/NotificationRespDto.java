package com.example.payroll_management_system.dto.responseDto;

import com.example.payroll_management_system.enums.NotificationType;

import java.time.Instant;

public record NotificationRespDto(
        Long notificationId,
        String title,
        NotificationType type,
        Boolean isRead,
        Instant createdAt
) {
}
