package com.example.payroll_management_system.dto.responseDto;

public record AdminDashboardRespDto(
        long totalUsers,
        long activeUsers,
        long totalEmployees,
        long onboardingEmployees,
        long activeEmployees,
        long retiredEmployees,
        long pendingLeaves,
        long runsDraft,
        long runsCalculated,
        long runsHrReview,
        long runsApproved,
        long runsPaid
) {
}
