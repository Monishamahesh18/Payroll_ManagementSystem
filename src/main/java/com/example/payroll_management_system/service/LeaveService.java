package com.example.payroll_management_system.service;

import com.example.payroll_management_system.dto.requestDto.LeaveDto;
import com.example.payroll_management_system.dto.responseDto.LeaveBalanceRespDto;
import com.example.payroll_management_system.dto.responseDto.LeaveRespDto;
import com.example.payroll_management_system.enums.LeaveStatus;
import com.example.payroll_management_system.enums.NotificationType;
import com.example.payroll_management_system.exceptions.InvalidCallException;
import com.example.payroll_management_system.exceptions.OwnershipException;
import com.example.payroll_management_system.exceptions.ResourceNotFoundException;
import com.example.payroll_management_system.mapper.LeaveMapper;
import com.example.payroll_management_system.model.Employee;
import com.example.payroll_management_system.model.LeaveRequest;
import com.example.payroll_management_system.repository.EmployeeRepository;
import com.example.payroll_management_system.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    // yearly leave quota in days, taken from application.properties
    @Value("${leave.yearly-quota:24}")
    private int yearlyQuota;

    @Transactional
    public void add(String email, LeaveDto leaveDto) {
        // Step 1: get the logged in employee from DB using the email of the token
        Employee employee = employeeRepository.getByUserEmail(email);
        if(employee == null)
            throw new ResourceNotFoundException("Employee Not Found");

        // Step 1.1: top manager has nobody above to approve, so leave cannot be applied
        if(employee.getManager() == null)
            throw new InvalidCallException("Leave cannot be applied because no manager is there to approve it");

        // Step 1.5: to date should not be before from date
        if(leaveDto.toDate().isBefore(leaveDto.fromDate()))
            throw new InvalidCallException("To date cannot be before from date");

        // Step 1.6: dates should not overlap with another pending or approved leave of this employee
        long count = leaveRequestRepository.countOverlappingLeaves(
                employee.getId(), LeaveStatus.REJECTED, leaveDto.fromDate(), leaveDto.toDate());
        if(count > 0)
            throw new InvalidCallException("Leave dates overlap with another leave");

        // Step 2: change LeaveDto to LeaveRequest entity using mapper
        LeaveRequest leave = LeaveMapper.convertDtoToEntity(leaveDto);

        // Step 3: attach the employee to leave
        leave.setEmployee(employee);

        // Step 3.5: new leave starts with PENDING status, manager will review it later
        leave.setStatus(LeaveStatus.PENDING);

        // Step 4: save leave in DB
        leaveRequestRepository.save(leave);

        // Step 5: tell the manager that a new leave request is waiting
        notificationService.create(employee.getManager().getUser(),
                "New leave request from " + employee.getFullName() + " (" + leaveDto.fromDate() + " to " + leaveDto.toDate() + ")",
                NotificationType.LEAVE);
        log.info("Leave requested by {}", email);
    }

    // leaves of the logged in employee
    public List<LeaveRespDto> getMine(String email, Integer page, Integer size) {
        // Step 1: get the logged in employee from DB using the email of the token
        Employee employee = employeeRepository.getByUserEmail(email);
        if(employee == null)
            throw new ResourceNotFoundException("Employee Not Found");

        // Step 2: create Pageable using page and size values thru PageRequest
        Pageable pageable = PageRequest.of(page, size);

        // Step 3: get the leaves of this employee (query gives the dto directly)
        return leaveRequestRepository.getLeaveByEmployeeId(employee.getId(), pageable);
    }

    // pending leaves of the team of the logged in manager
    public List<LeaveRespDto> getPending(String managerEmail, Integer page, Integer size) {
        // Step 1: get the logged in manager from DB using the email of the token
        Employee manager = employeeRepository.getByUserEmail(managerEmail);
        if(manager == null)
            throw new ResourceNotFoundException("Manager Id Invalid");

        // Step 2: create Pageable using page and size values thru PageRequest
        Pageable pageable = PageRequest.of(page, size);

        // Step 3: get only the PENDING leaves of this manager's team
        return leaveRequestRepository.getLeaveByManagerIdAndStatus(manager.getId(), LeaveStatus.PENDING, pageable);
    }

    // all leaves of the team of the logged in manager. status is optional - null gives every status
    public List<LeaveRespDto> getTeamLeaves(String managerEmail, LeaveStatus status, Integer page, Integer size) {
        // Step 1: get the logged in manager from DB using the email of the token
        Employee manager = employeeRepository.getByUserEmail(managerEmail);
        if(manager == null)
            throw new ResourceNotFoundException("Manager Id Invalid");

        // Step 2: create Pageable using page and size values thru PageRequest
        Pageable pageable = PageRequest.of(page, size);

        // Step 3: get the leaves of the team (query gives the dto directly)
        return leaveRequestRepository.getLeaveByManagerId(manager.getId(), status, pageable);
    }

    // year is optional - current year is taken when it is not given
    public LeaveBalanceRespDto getBalance(String email, Integer year) {
        // Step 1: get the logged in employee from DB using the email of the token
        Employee employee = employeeRepository.getByUserEmail(email);
        if(employee == null)
            throw new ResourceNotFoundException("Employee Not Found");
        Long employeeId = employee.getId();

        // Step 2: first and last day of the year
        int balanceYear = (year == null) ? LocalDate.now().getYear() : year;
        LocalDate yearStart = LocalDate.of(balanceYear, 1, 1);
        LocalDate yearEnd = LocalDate.of(balanceYear, 12, 31);

        // Step 3: approved and pending leaves of this year
        List<LeaveRequest> approved = leaveRequestRepository
                .getLeavesInPeriod(employeeId, LeaveStatus.APPROVED, yearStart, yearEnd);
        List<LeaveRequest> pending = leaveRequestRepository
                .getLeavesInPeriod(employeeId, LeaveStatus.PENDING, yearStart, yearEnd);

        // Step 4: count the days and find how many days are left
        long approvedDays = countDays(approved, yearStart, yearEnd);
        long pendingDays = countDays(pending, yearStart, yearEnd);

        return new LeaveBalanceRespDto(employeeId, employee.getFullName(), balanceYear, yearlyQuota,
                approvedDays, pendingDays, yearlyQuota - approvedDays - pendingDays);
    }

    // a leave that starts in last year or ends in next year is counted only for the days inside this year
    private long countDays(List<LeaveRequest> list, LocalDate yearStart, LocalDate yearEnd) {
        long days = 0;
        for(LeaveRequest leave : list){
            LocalDate from = leave.getFromDate().isBefore(yearStart) ? yearStart : leave.getFromDate();
            LocalDate to = leave.getToDate().isAfter(yearEnd) ? yearEnd : leave.getToDate();
            days += ChronoUnit.DAYS.between(from, to) + 1;
        }
        return days;
    }

    // the employee takes back his own leave request. only a pending leave can be cancelled
    @Transactional
    public void cancel(Long leaveId, String email) {
        // Step 1: get the leave from DB using the id given in url
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(()-> new ResourceNotFoundException("Leave Request Not Found"));

        // Step 2: get the logged in employee from DB using the email of the token
        Employee employee = employeeRepository.getByUserEmail(email);
        if(employee == null)
            throw new ResourceNotFoundException("Employee Not Found");

        // Step 3: the leave should belong to this employee
        if(!leave.getEmployee().getId().equals(employee.getId()))
            throw new OwnershipException("Operation not allowed! contact admin");

        // Step 4: only a pending leave can be cancelled
        if(leave.getStatus() != LeaveStatus.PENDING)
            throw new InvalidCallException("Only a pending leave can be cancelled");

        // Step 5: delete the leave. its days go back to the balance
        leaveRequestRepository.delete(leave);

        // Step 6: tell the manager and keep an audit entry (done by the employee)
        if(employee.getManager() != null)
            notificationService.create(employee.getManager().getUser(),
                    employee.getFullName() + " cancelled the leave request from " + leave.getFromDate() + " to " + leave.getToDate(),
                    NotificationType.LEAVE);
        auditService.log(employee.getUser(), "LEAVE_CANCELLED", "LEAVE_REQUEST", leaveId);

        log.info("Leave {} is cancelled by {}", leaveId, email);
    }

    @Transactional
    public void review(Long leaveId, String managerEmail, LeaveStatus status) {
        // Step 1: get the leave from DB using the id given in url
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(()-> new ResourceNotFoundException("Leave Request Not Found"));

        // Step 2: get the logged in manager from DB using the email of the token
        Employee manager = employeeRepository.getByUserEmail(managerEmail);
        if(manager == null)
            throw new ResourceNotFoundException("Manager Id Invalid");

        // Step 3: only APPROVED or REJECTED can be given
        if(status == LeaveStatus.PENDING)
            throw new InvalidCallException("Status must be APPROVED or REJECTED");

        // Step 4: only a pending leave can be reviewed
        if(leave.getStatus() != LeaveStatus.PENDING)
            throw new InvalidCallException("Leave is already reviewed");

        // Step 5: the leave should belong to the team of this manager
        Employee leaveManager = leave.getEmployee().getManager();
        if(leaveManager == null || !leaveManager.getId().equals(manager.getId()))
            throw new OwnershipException("Operation not allowed! contact admin");

        // Step 6: update status, set reviewer and save in DB
        leave.setStatus(status);
        leave.setReviewedBy(manager);
        leaveRequestRepository.save(leave);

        // Step 7: tell the employee and keep an audit entry (done by the manager)
        notificationService.create(leave.getEmployee().getUser(),
                "Your leave from " + leave.getFromDate() + " to " + leave.getToDate() + " is " + status,
                NotificationType.LEAVE);

        auditService.log(manager.getUser(), "LEAVE_" + status, "LEAVE_REQUEST", leave.getId());

        log.info("Leave {} is {} by {}", leaveId, status, managerEmail);
    }
}
