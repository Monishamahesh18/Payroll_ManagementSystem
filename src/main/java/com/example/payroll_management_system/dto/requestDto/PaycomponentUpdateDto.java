package com.example.payroll_management_system.dto.requestDto;

import jakarta.validation.constraints.Positive;

// only the value of a pay component can be changed
public record PaycomponentUpdateDto(
        @Positive(message = "Value must be greater than 0")
        Double value
) {
}
