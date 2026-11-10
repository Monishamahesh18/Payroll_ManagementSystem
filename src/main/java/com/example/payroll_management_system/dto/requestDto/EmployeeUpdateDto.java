package com.example.payroll_management_system.dto.requestDto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

// admin updates employee details. every field is optional, only the fields sent are updated
public record EmployeeUpdateDto(
        @Size(max = 255, message = "Full name can be maximum 255 characters")
        String fullName,

        @Size(max = 255, message = "Department can be maximum 255 characters")
        String department,

        @Positive(message = "Basic must be greater than 0")
        Double basic,

        @PositiveOrZero(message = "HRA cannot be negative")
        Double hra,

        @PositiveOrZero(message = "Allowance cannot be negative")
        Double allowance,

        @Size(max = 30, message = "Bank account can be maximum 30 characters")
        String bankAcc
) {
}
