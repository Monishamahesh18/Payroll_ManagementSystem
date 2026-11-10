package com.example.payroll_management_system.controller;

import com.example.payroll_management_system.dto.requestDto.LeaveDto;
import com.example.payroll_management_system.dto.responseDto.LeaveBalanceRespDto;
import com.example.payroll_management_system.dto.responseDto.LeaveRespDto;
import com.example.payroll_management_system.enums.LeaveStatus;
import com.example.payroll_management_system.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")
public class LeaveController {

    private final LeaveService leaveService;

    // the employee is the logged in user, so no id in the url
    // Done by Employee
    @PostMapping("/api/leave/add")
    public void postLeave(Principal principal,
                          @Valid @RequestBody LeaveDto leaveDto){
        leaveService.add(principal.getName(), leaveDto);
    }

    // leaves of the logged in employee
    // Done by Employee
    @GetMapping("/api/leave/my")
    public List<LeaveRespDto> getMine(Principal principal,
                                      @RequestParam(name = "page" , required = false, defaultValue = "0") Integer page,
                                      @RequestParam(name = "size", required = false, defaultValue = "20") Integer size){
        return leaveService.getMine(principal.getName(), page, size);
    }

    // pending leaves of the team of the logged in manager
    // Done by Manager
    @GetMapping("/api/leave/pending")
    public List<LeaveRespDto> getPending(Principal principal,
                                         @RequestParam(name = "page" , required = false, defaultValue = "0") Integer page,
                                         @RequestParam(name = "size", required = false, defaultValue = "20") Integer size){
        return leaveService.getPending(principal.getName(), page, size);
    }

    // status is optional - nothing given means every status
    // Done by Manager
    @GetMapping("/api/leave/team")
    public List<LeaveRespDto> getTeamLeaves(Principal principal,
                                            @RequestParam(name = "status", required = false) LeaveStatus status,
                                            @RequestParam(name = "page" , required = false, defaultValue = "0") Integer page,
                                            @RequestParam(name = "size", required = false, defaultValue = "20") Integer size){
        return leaveService.getTeamLeaves(principal.getName(), status, page, size);
    }

    // year is optional - current year is taken when it is not given
    // Done by Employee
    @GetMapping("/api/leave/balance")
    public LeaveBalanceRespDto getBalance(Principal principal,
                                          @RequestParam(name = "year", required = false) Integer year){
        return leaveService.getBalance(principal.getName(), year);
    }

    // the employee takes back his own pending leave
    // Done by Employee
    @DeleteMapping("/api/leave/cancel/{leaveId}")
    public void cancel(@PathVariable Long leaveId,
                       Principal principal){
        leaveService.cancel(leaveId, principal.getName());
    }

    // the manager is the logged in user. only the manager of that employee can review
    // Done by Manager
    @PutMapping("/api/leave/review/{leaveId}")
    public void review(@PathVariable Long leaveId,
                       @RequestParam("status") LeaveStatus status,
                       Principal principal){
        leaveService.review(leaveId, principal.getName(), status);
    }
}
