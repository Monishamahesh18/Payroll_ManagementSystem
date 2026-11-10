package com.example.payroll_management_system.controller;

import com.example.payroll_management_system.dto.responseDto.AuditLogRespDto;
import com.example.payroll_management_system.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")
public class AuditController {

    private final AuditService auditService;

    // every parameter is optional. from and to are dates (yyyy-MM-dd), to date is included
    // Done by Admin / Processor - activate the check after security config
    @GetMapping("/api/audit/all")
    public List<AuditLogRespDto> getAll(@RequestParam(name = "userId", required = false) Long userId,
                                        @RequestParam(name = "action", required = false) String action,
                                        @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                        @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                        @RequestParam(name = "page" , required = false, defaultValue = "0") Integer page,
                                        @RequestParam(name = "size", required = false, defaultValue = "20") Integer size){
        return auditService.getAll(userId, action, from, to, page, size);
    }
}
