package com.example.payroll_management_system.dto.responseDto;

import com.example.payroll_management_system.enums.RunStatus;

public record ProcessorDashboardRespDto(
        long activeEmployees,
        long activePayComponents,
        long submittedTimesheets,
        long lockedTimesheets,
        long runsDraft,
        long runsCalculated,
        long runsHrReview,
        long runsApproved,
        long runsPaid,
        Long latestRunId,
        RunStatus latestRunStatus
) {
}
