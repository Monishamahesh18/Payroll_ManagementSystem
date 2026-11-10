package com.example.payroll_management_system.controller;

import com.example.payroll_management_system.dto.responseDto.ComplianceRespDto;
import com.example.payroll_management_system.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")
public class ReportController {

    private final ReportService reportService;

    // statutory items (PF, tax withholding etc.) of one run with total amount
    // Done by Admin - activate the check after security config
    @GetMapping("/api/report/compliance/run/{runId}")
    public List<ComplianceRespDto> getComplianceByRunId(@PathVariable Long runId){
        return reportService.getComplianceByRunId(runId);
    }
}
