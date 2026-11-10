package com.example.payroll_management_system.controller;

import com.example.payroll_management_system.dto.requestDto.AdminDto;
import com.example.payroll_management_system.dto.requestDto.PasswordDto;
import com.example.payroll_management_system.dto.responseDto.UserRespDto;
import com.example.payroll_management_system.enums.EmployeeStatus;
import com.example.payroll_management_system.enums.Role;
import com.example.payroll_management_system.service.EmployeeService;
import com.example.payroll_management_system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")
public class AdminController {

    private final UserService userService;
    private final EmployeeService employeeService;

    @PostMapping("/api/admin/add")
    public void addAdmin(@Valid @RequestBody AdminDto adminDto){
        userService.addAdmin(adminDto);
    }

    // Done by Admin - activate the check after security config
    @GetMapping("/api/user/all")
    public List<UserRespDto> getAllUsers(@RequestParam(name = "role", required = false) Role role,
                                         @RequestParam(name = "page" , required = false, defaultValue = "0") Integer page,
                                         @RequestParam(name = "size", required = false, defaultValue = "20") Integer size){
        return userService.getAll(role, page, size);
    }

    // Done by Admin - activate the check after security config
    @PutMapping("/api/user/deactivate/{userId}")
    public void deactivateUser(@PathVariable long userId){
        userService.deactivate(userId);
    }

    // Done by Admin - activate the check after security config
    @PutMapping("/api/user/activate/{userId}")
    public void activateUser(@PathVariable long userId){
        userService.activate(userId);
    }

    // Done by Admin - activate the check after security config
    @PutMapping("/api/user/role/{userId}")
    public void changeRole(@PathVariable long userId,
                           @RequestParam("role") Role role){
        userService.changeRole(userId, role);
    }

    // Done by Admin - activate the check after security config
    @PutMapping("/api/user/password/{userId}")
    public void changePassword(@PathVariable long userId,
                               @Valid @RequestBody PasswordDto passwordDto){
        userService.changePassword(userId, passwordDto);
    }

    // Done by Admin - activate the check after security config
    @PutMapping("/api/status/update/{employeeId}")
    public void updateStatus(@PathVariable long employeeId,
                             @RequestParam("status") EmployeeStatus status){
        employeeService.updateStatus(employeeId, status);
    }
}
