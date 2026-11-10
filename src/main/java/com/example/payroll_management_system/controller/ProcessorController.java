package com.example.payroll_management_system.controller;

import com.example.payroll_management_system.dto.requestDto.AdminDto;
import com.example.payroll_management_system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")
public class ProcessorController {

    private final UserService userService;

    // Done by Admin - activate the check after security config
    @PostMapping("/api/processor/add")
    public void addProcessor(@Valid @RequestBody AdminDto adminDto){
        userService.addProcessor(adminDto);
    }
}
