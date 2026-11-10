package com.example.payroll_management_system.controller;

import com.example.payroll_management_system.dto.responseDto.AdminDashboardRespDto;
import com.example.payroll_management_system.dto.responseDto.ManagerDashboardRespDto;
import com.example.payroll_management_system.dto.responseDto.ProcessorDashboardRespDto;
import com.example.payroll_management_system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")
public class DashboardController {

    private final DashboardService dashboardService;

    // Done by Admin - activate the check after security config
    @GetMapping("/api/dashboard/admin")
    public AdminDashboardRespDto getAdminDashboard(){
        return dashboardService.getAdminDashboard();
    }

    // Done by Processor - activate the check after security config
    @GetMapping("/api/dashboard/processor")
    public ProcessorDashboardRespDto getProcessorDashboard(){
        return dashboardService.getProcessorDashboard();
    }

    // the manager is the logged in user, so no id in the url
    // Done by Manager
    @GetMapping("/api/dashboard/manager")
    public ManagerDashboardRespDto getManagerDashboard(Principal principal){
        return dashboardService.getManagerDashboard(principal.getName());
    }
}
