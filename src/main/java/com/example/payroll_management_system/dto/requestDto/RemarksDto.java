package com.example.payroll_management_system.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// reason for sending a payroll run back for reprocess
public record RemarksDto(
        @NotBlank(message = "Remarks is required")
        @NotNull(message = "Remarks is required")
        @Size(max = 255, message = "Remarks can be maximum 255 characters")
        String remarks
) {
}
