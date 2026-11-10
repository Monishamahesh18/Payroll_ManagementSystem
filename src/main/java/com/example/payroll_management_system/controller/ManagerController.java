package com.example.payroll_management_system.controller;

import com.example.payroll_management_system.dto.requestDto.EmployeeDto;
import com.example.payroll_management_system.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")
public class ManagerController {

    private final EmployeeService employeeService;

    // Done by Admin - activate the check after security config
    @PostMapping("/api/manager/add")
    public void add(@Valid @RequestBody EmployeeDto employeeDto){
         employeeService.addManager(employeeDto);
    }
}
