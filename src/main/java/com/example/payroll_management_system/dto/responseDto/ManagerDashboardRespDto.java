package com.example.payroll_management_system.dto.responseDto;

import com.example.payroll_management_system.enums.RunStatus;

public record ManagerDashboardRespDto(
        Long managerId,
        String managerName,
        long teamSize,
        long activeTeamMembers,
        long pendingLeaves,
        Long latestRunId,
        RunStatus latestRunStatus,
        double latestRunTeamNetPay
) {
}
