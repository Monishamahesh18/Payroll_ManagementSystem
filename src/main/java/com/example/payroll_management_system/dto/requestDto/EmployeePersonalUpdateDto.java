package com.example.payroll_management_system.dto.requestDto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

// employee updates own personal details. salary fields are not here, so employee cannot change salary
// every field is optional, only the fields sent are updated
public record EmployeePersonalUpdateDto(
        @Size(max = 20, message = "Phone can be maximum 20 characters")
        String phone,

        @Size(max = 255, message = "Address can be maximum 255 characters")
        String address,

        @Size(max = 30, message = "Bank account can be maximum 30 characters")
        String bankAcc,

        // tax withholding in percent (0 to 100)
        @PositiveOrZero(message = "Tax withholding cannot be negative")
        Double taxWithholding
) {
}
