package com.example.payroll_management_system.controller;

import com.example.payroll_management_system.dto.responseDto.PayslipDetailRespDto;
import com.example.payroll_management_system.dto.responseDto.PayslipRespDto;
import com.example.payroll_management_system.dto.responseDto.PayslipStatRespDto;
import com.example.payroll_management_system.service.PayslipService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")
public class PayslipController {
    private final PayslipService payslipService;

    // Done by Admin / Processor - activate the check after security config
    @GetMapping("/api/payslip/run/{runId}")
    public List<PayslipRespDto> getByRunId(@PathVariable Long runId,
                                           @RequestParam(name = "page" , required = false, defaultValue = "0") Integer page,
                                           @RequestParam(name = "size", required = false, defaultValue = "20") Integer size){
        return payslipService.getByRunId(runId, page, size);
    }

// Done by Employee
    @GetMapping("/api/payslip/employee/{employeeId}")
    public List<PayslipRespDto> getByEmployeeId(@PathVariable Long employeeId,
                                                @RequestParam(name = "page" , required = false, defaultValue = "0") Integer page,
                                                @RequestParam(name = "size", required = false, defaultValue = "20") Integer size){
        return payslipService.getByEmployeeId(employeeId, page, size);
    }

    // the owner of the payslip, or an admin or a processor
    @GetMapping("/api/payslip/{id}")
    public PayslipDetailRespDto getById(@PathVariable Long id,
                                        Principal principal){
        return payslipService.getById(id, principal.getName());
    }

    // payslips of the team of the logged in manager. run id is optional - nothing given means every run
    // Done by Manager
    @GetMapping("/api/payslip/team")
    public List<PayslipRespDto> getByManagerId(Principal principal,
                                               @RequestParam(name = "runId", required = false) Long runId,
                                               @RequestParam(name = "page" , required = false, defaultValue = "0") Integer page,
                                               @RequestParam(name = "size", required = false, defaultValue = "20") Integer size){
        return payslipService.getByManagerEmail(principal.getName(), runId, page, size);
    }

    // total gross, deduction and net of one run
    // Done by Admin / Processor - activate the check after security config
    @GetMapping("/api/payslip/stat/run/{runId}")
    public PayslipStatRespDto getStat(@PathVariable Long runId){
        return payslipService.getStat(runId);
    }

    // payslips of the logged in employee - version 1: every payslip
    // Done by Employee / Manager
    @GetMapping("/api/payslip/v1/my")
    public List<PayslipRespDto> getMine(Principal principal,
                                        @RequestParam(name = "page" , required = false, defaultValue = "0") Integer page,
                                        @RequestParam(name = "size", required = false, defaultValue = "20") Integer size){
        return payslipService.getMine(principal.getName(), page, size);
    }

    // payslips of the logged in employee - version 2: only the payslips of one year
    // Done by Employee / Manager
    @GetMapping("/api/payslip/v2/my")
    public List<PayslipRespDto> getMineByYear(Principal principal,
                                              @RequestParam("year") Integer year,
                                              @RequestParam(name = "page" , required = false, defaultValue = "0") Integer page,
                                              @RequestParam(name = "size", required = false, defaultValue = "20") Integer size){
        return payslipService.getMineByYear(principal.getName(), year, page, size);
    }
}
