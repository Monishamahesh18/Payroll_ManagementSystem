package com.example.payroll_management_system.controller;

import com.example.payroll_management_system.dto.requestDto.PayrollrunDto;
import com.example.payroll_management_system.dto.requestDto.RemarksDto;
import com.example.payroll_management_system.dto.responseDto.PayrollVerifyRespDto;
import com.example.payroll_management_system.dto.responseDto.PayrollrunRespDto;
import com.example.payroll_management_system.enums.RunStatus;
import com.example.payroll_management_system.service.PayrollrunService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")
public class PayrollrunController {

    private final PayrollrunService payrollrunService;

    // Done by Processor - activate the check after security config
    @PostMapping("/api/payrollrun/add")
    public void add(@Valid @RequestBody PayrollrunDto payrollrunDto){

        payrollrunService.add(payrollrunDto);
    }

    // Done by Admin / Processor - activate the check after security config
    @GetMapping("/api/payrollrun/all")
    public List<PayrollrunRespDto> getAll(@RequestParam(name = "page" , required = false, defaultValue = "0") Integer page,
                                          @RequestParam(name = "size", required = false, defaultValue = "20") Integer size){
        return payrollrunService.getAll(page, size);
    }

    // Done by Admin / Processor - activate the check after security config
    @GetMapping("/api/payrollrun/{id}")
    public PayrollrunRespDto getById(@PathVariable Long id){
        return payrollrunService.getById(id);
    }

    // Done by Admin / Processor - activate the check after security config
    @GetMapping("/api/payrollrun/status/{status}")
    public List<PayrollrunRespDto> getByStatus(@PathVariable RunStatus status,
                                               @RequestParam(name = "page" , required = false, defaultValue = "0") Integer page,
                                               @RequestParam(name = "size", required = false, defaultValue = "20") Integer size){
        return payrollrunService.getByStatus(status, page, size);
    }

    // processor checks the calculated run (missing bank account, net pay zero or less)
    // Done by Processor - activate the check after security config
    @GetMapping("/api/payrollrun/verify/{runId}")
    public PayrollVerifyRespDto verify(@PathVariable Long runId){
        return payrollrunService.verify(runId);
    }

    // the processor is the logged in user, so no user id in the url
    // Done by Processor
    @PutMapping("/api/payrollrun/calculate/{runId}")
    public void calculate(@PathVariable Long runId,
                          Principal principal){
        payrollrunService.calculate(runId, principal.getName());
    }

    // Done by Processor
    @PutMapping("/api/payrollrun/submit/{runId}")
    public void submit(@PathVariable Long runId,
                       Principal principal){
        payrollrunService.submit(runId, principal.getName());
    }

    // Done by Admin
    @PutMapping("/api/payrollrun/approve/{runId}")
    public void approve(@PathVariable Long runId,
                        Principal principal){
        payrollrunService.approve(runId, principal.getName());
    }

    // Done by Processor
    @PutMapping("/api/payrollrun/pay/{runId}")
    public void pay(@PathVariable Long runId,
                    Principal principal){
        payrollrunService.pay(runId, principal.getName());
    }

    // Done by Processor / Admin
    @PutMapping("/api/payrollrun/reprocess/{runId}")
    public void reprocess(@PathVariable Long runId,
                          @Valid @RequestBody RemarksDto remarksDto,
                          Principal principal){
        payrollrunService.reprocess(runId, principal.getName(), remarksDto);
    }
}
