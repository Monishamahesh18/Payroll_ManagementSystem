package com.example.payroll_management_system.controller;

import com.example.payroll_management_system.dto.requestDto.TimesheetDto;
import com.example.payroll_management_system.dto.requestDto.TimesheetUpdateDto;
import com.example.payroll_management_system.dto.responseDto.TimeSheetRespDto;
import com.example.payroll_management_system.service.TimesheetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")
public class TimesheetController {

    private final TimesheetService timesheetService;

    // Done by Employee / Manager
    @PostMapping("/api/timesheet/add")
    public void postTimesheet(Principal principal,
                              @Valid @RequestBody TimesheetDto timesheetDto){
        timesheetService.add(principal.getName(), timesheetDto);
    }

    // timesheets of the logged in employee
    // Done by Employee / Manager
    @GetMapping("/api/timesheet/my")
    public List<TimeSheetRespDto> getMine(Principal principal,
                                          @RequestParam(name = "page" , required = false, defaultValue = "0") Integer page,
                                          @RequestParam(name = "size", required = false, defaultValue = "20") Integer size){
        return timesheetService.getMine(principal.getName(), page, size);
    }

    // timesheets of all employees between two dates (yyyy-MM-dd)
    // Done by Processor - activate the check after security config
    @GetMapping("/api/timesheet/period")
    public List<TimeSheetRespDto> getByPeriod(@RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                              @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                              @RequestParam(name = "page" , required = false, defaultValue = "0") Integer page,
                                              @RequestParam(name = "size", required = false, defaultValue = "20") Integer size){
        return timesheetService.getByPeriod(from, to, page, size);
    }

    // Done by Employee / Manager (only own timesheet)
    @PutMapping("/api/timesheet/{id}")
    public void update(@PathVariable Long id,
                       @Valid @RequestBody TimesheetUpdateDto timesheetUpdateDto,
                       Principal principal){
        timesheetService.update(id, timesheetUpdateDto, principal.getName());
    }

    // Done by Processor - activate the check after security config
    @PutMapping("/api/timesheet/lock")
    public void lock(@RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                     @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to){
        timesheetService.lock(from, to);
    }
}
