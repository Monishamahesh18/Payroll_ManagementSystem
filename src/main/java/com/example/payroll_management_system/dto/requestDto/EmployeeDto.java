package com.example.payroll_management_system.dto.requestDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// email and password go to User table, remaining fields go to Employee table
public record EmployeeDto(
        @NotBlank(message = "Employee code is required")
        @NotNull(message = "Employee code is required")
        String empCode,

        @NotBlank(message = "Full name is required")
        @NotNull(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Department is required")
        @NotNull(message = "Department is required")
        String department,

        @NotNull(message = "Join date is required")
        LocalDate joinDate,

        @NotNull(message = "Basic is required")
        @Positive(message = "Basic must be greater than 0")
        Double basic,

        @NotNull(message = "HRA is required")
        @PositiveOrZero(message = "HRA cannot be negative")
        Double hra,

        @NotNull(message = "Allowance is required")
        @PositiveOrZero(message = "Allowance cannot be negative")
        Double allowance,

        @Size(max = 30, message = "Bank account can be maximum 30 characters")
        String bankAcc,

        @Size(max = 20, message = "Phone can be maximum 20 characters")
        String phone,

        @Size(max = 255, message = "Address can be maximum 255 characters")
        String address,

        @NotBlank(message = "Email is required")
        @NotNull(message = "Email is required")
        @Email(message = "Email seems invalid")
        String email,

        @NotBlank(message = "Password is required")
        @NotNull(message = "Password is required")
        String password
) {
}
