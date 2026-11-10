package com.example.payroll_management_system.service;

import com.example.payroll_management_system.dto.responseDto.ComplianceRespDto;
import com.example.payroll_management_system.exceptions.ResourceNotFoundException;
import com.example.payroll_management_system.repository.PayrollrunRepository;
import com.example.payroll_management_system.repository.PayslipItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final PayslipItemRepository payslipItemRepository;
    private final PayrollrunRepository payrollrunRepository;

    public List<ComplianceRespDto> getComplianceByRunId(Long runId) {
        // Step 1: check the payroll run id is there in DB
        payrollrunRepository.findById(runId)
                .orElseThrow(()-> new ResourceNotFoundException("Payroll Run Not Found"));

        // Step 2: statutory items of this run, total amount for each (query gives the dto directly)
        return payslipItemRepository.getComplianceByRunId(runId);
    }
}
