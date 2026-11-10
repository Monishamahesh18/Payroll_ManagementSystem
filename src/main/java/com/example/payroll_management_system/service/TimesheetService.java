package com.example.payroll_management_system.service;

import com.example.payroll_management_system.dto.requestDto.TimesheetDto;
import com.example.payroll_management_system.dto.requestDto.TimesheetUpdateDto;
import com.example.payroll_management_system.dto.responseDto.TimeSheetRespDto;
import com.example.payroll_management_system.enums.EmployeeStatus;
import com.example.payroll_management_system.enums.TimesheetStatus;
import com.example.payroll_management_system.exceptions.InvalidCallException;
import com.example.payroll_management_system.exceptions.OwnershipException;
import com.example.payroll_management_system.exceptions.ResourceNotFoundException;
import com.example.payroll_management_system.mapper.TimesheetMapper;
import com.example.payroll_management_system.model.Employee;
import com.example.payroll_management_system.model.Timesheet;
import com.example.payroll_management_system.repository.EmployeeRepository;
import com.example.payroll_management_system.repository.TimesheetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimesheetService {

    private final TimesheetRepository timesheetRepository;
    private final EmployeeRepository employeeRepository;

    public void add(String email, TimesheetDto timesheetDto) {
        // Step 1: get the logged in employee from DB using the email of the token
        Employee employee = employeeRepository.getByUserEmail(email);
        if(employee == null)
            throw new ResourceNotFoundException("Employee Not Found");

        // Step 1.1: retired employee cannot give timesheet
        if(employee.getStatus() == EmployeeStatus.RETIRED)
            throw new InvalidCallException("Retired employee cannot submit timesheet");

        // Step 1.5: hours should be between 0 and 24, if not throw exception
        if(timesheetDto.hours() <= 0 || timesheetDto.hours() > 24)
            throw new InvalidCallException("Hours must be between 0 and 24");

        // Step 1.6: one employee can give only one timesheet for one day
        if(timesheetRepository.existsByEmployeeIdAndWorkDate(employee.getId(), timesheetDto.workDate()))
            throw new InvalidCallException("Timesheet already submitted for this date");

        // Step 2: change TimesheetDto to Timesheet entity using mapper
        Timesheet timesheet = TimesheetMapper.convertDtoToEntity(timesheetDto);

        // Step 3: attach the employee to timesheet
        timesheet.setEmployee(employee);

        // Step 3.5: new timesheet starts with SUBMITTED status
        timesheet.setStatus(TimesheetStatus.SUBMITTED);

        // Step 4: save timesheet in DB
        timesheetRepository.save(timesheet);
        log.info("Timesheet added by {}", email);
    }

    // timesheets of the logged in employee
    public List<TimeSheetRespDto> getMine(String email, Integer page, Integer size) {
        // Step 1: get the logged in employee from DB using the email of the token
        Employee employee = employeeRepository.getByUserEmail(email);
        if(employee == null)
            throw new ResourceNotFoundException("Employee Not Found");

        // Step 2: create Pageable using page and size values thru PageRequest
        Pageable pageable = PageRequest.of(page, size);

        // Step 3: get the timesheets of this employee (query gives the dto directly)
        return timesheetRepository.getTimesheetByEmployeeId(employee.getId(), pageable);
    }

    public void update(Long id, TimesheetUpdateDto dto, String email) {
        // Step 1: get the timesheet from DB using the id given in url
        Timesheet timesheet = timesheetRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Timesheet Not Found"));

        // Step 1.5: the timesheet should belong to the logged in employee
        if(!timesheet.getEmployee().getUser().getEmail().equals(email))
            throw new OwnershipException("Operation not allowed! contact admin");

        // Step 2: locked timesheet cannot be edited
        if(timesheet.getStatus() == TimesheetStatus.LOCKED)
            throw new InvalidCallException("Timesheet is locked and cannot be edited");

        // Step 3: hours should be between 0 and 24, if not throw exception
        if(dto.hours() <= 0 || dto.hours() > 24)
            throw new InvalidCallException("Hours must be between 0 and 24");

        // Step 4: update hours and save in DB
        timesheet.setHours(dto.hours());
        timesheetRepository.save(timesheet);
    }

    // processor locks all timesheets of a period before payroll
    @Transactional
    public void lock(LocalDate from, LocalDate to) {
        // Step 1: from date should not be after to date
        if(from.isAfter(to))
            throw new InvalidCallException("From date cannot be after to date");

        // Step 2: get all timesheets between the two dates
        List<Timesheet> list = timesheetRepository.findByWorkDateBetween(from, to);
        if(list.isEmpty())
            throw new InvalidCallException("No timesheets found in the given period");

        // Step 3: lock each one and save all
        list.forEach(timesheet -> timesheet.setStatus(TimesheetStatus.LOCKED));
        timesheetRepository.saveAll(list);
        log.info("Timesheets from {} to {} are locked", from, to);
    }

    // timesheets of all employees in a period (processor checks them before locking)
    public List<TimeSheetRespDto> getByPeriod(LocalDate from, LocalDate to, Integer page, Integer size) {
        // Step 1: from date should not be after to date
        if(from.isAfter(to))
            throw new InvalidCallException("From date cannot be after to date");

        // Step 2: create Pageable using page and size values thru PageRequest
        Pageable pageable = PageRequest.of(page, size);

        // Step 3: get the timesheets (query gives the dto directly)
        return timesheetRepository.getTimesheetByPeriod(from, to, pageable);
    }
}
