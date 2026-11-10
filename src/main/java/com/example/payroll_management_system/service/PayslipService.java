package com.example.payroll_management_system.service;

import com.example.payroll_management_system.dto.responseDto.PayslipDetailRespDto;
import com.example.payroll_management_system.dto.responseDto.PayslipRespDto;
import com.example.payroll_management_system.dto.responseDto.PayslipStatRespDto;
import com.example.payroll_management_system.enums.PayStatus;
import com.example.payroll_management_system.enums.Role;
import com.example.payroll_management_system.exceptions.InvalidCallException;
import com.example.payroll_management_system.exceptions.OwnershipException;
import com.example.payroll_management_system.exceptions.ResourceNotFoundException;
import com.example.payroll_management_system.mapper.PayslipMapper;
import com.example.payroll_management_system.model.Employee;
import com.example.payroll_management_system.model.Payslip;
import com.example.payroll_management_system.model.PayslipItem;
import com.example.payroll_management_system.model.User;
import com.example.payroll_management_system.repository.EmployeeRepository;
import com.example.payroll_management_system.repository.PayrollrunRepository;
import com.example.payroll_management_system.repository.PayslipItemRepository;
import com.example.payroll_management_system.repository.PayslipRepository;
import com.example.payroll_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayslipService {

    private final PayslipRepository payslipRepository;
    private final PayrollrunRepository payrollrunRepository;
    private final EmployeeRepository employeeRepository;
    private final PayslipItemRepository payslipItemRepository;
    private final UserRepository userRepository;

    public List<PayslipRespDto> getByRunId(Long runId, Integer page, Integer size) {
        // Step 1: check the payroll run id is there in DB
        payrollrunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll Run Not Found"));

        // Step 2: create Pageable using page and size values thru PageRequest
        Pageable pageable = PageRequest.of(page, size);

        // Step 3: get the payslips of this run (query gives the dto directly)
        return payslipRepository.getPayslipByRunId(runId, pageable);
    }

    public List<PayslipRespDto> getByEmployeeId(Long employeeId, Integer page, Integer size) {
        // Step 1: check the employee id is there in DB
        employeeRepository.findById(employeeId)
                .orElseThrow(()-> new ResourceNotFoundException("Employee Not Found"));

        // Step 2: create Pageable using page and size values thru PageRequest
        Pageable pageable = PageRequest.of(page, size);

        // Step 3: get the payslips of this employee (query gives the dto directly)
        return payslipRepository.getPayslipByEmployeeId(employeeId, pageable);
    }

    // the owner of the payslip can see it. admin and processor can see every payslip
    public PayslipDetailRespDto getById(Long id, String email) {
        // Step 1: get the payslip from DB using the id given in url
        Payslip payslip = payslipRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Payslip Not Found"));

        // Step 1.5: is the logged in user the owner, an admin or a processor, or the manager of the owner?
        User viewer = userRepository.loadUserByUsername(email, true);
        boolean isStaff = viewer != null && (viewer.getRole() == Role.ADMIN || viewer.getRole() == Role.PROCESSOR);
        boolean isOwner = payslip.getEmployee().getUser().getEmail().equals(email);
        Employee ownerManager = payslip.getEmployee().getManager();
        boolean isTeamManager = ownerManager != null && ownerManager.getUser() != null
                && ownerManager.getUser().getEmail().equals(email);
        if(!isStaff && !isOwner && !isTeamManager)
            throw new OwnershipException("Operation not allowed! contact admin");

        // Step 2: get the item lines (earnings, deductions, benefits) of this payslip
        List<PayslipItem> items = payslipItemRepository.findByPayslipIdOrderByIdAsc(id);

        // Step 3: change Payslip entity and its items to PayslipDetailRespDto using mapper
        return PayslipMapper.convertEntityToDetailDto(payslip, items);
    }

    // payslips of the team of the logged in manager. run id is optional - null gives every run
    public List<PayslipRespDto> getByManagerEmail(String managerEmail, Long runId, Integer page, Integer size) {
        // Step 1: get the logged in manager from DB using the email of the token
        Employee manager = employeeRepository.getByUserEmail(managerEmail);
        if(manager == null)
            throw new ResourceNotFoundException("Manager Id Invalid");

        // Step 2: if run id is given, check it is there in DB
        if(runId != null)
            payrollrunRepository.findById(runId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payroll Run Not Found"));

        // Step 3: create Pageable using page and size values thru PageRequest
        Pageable pageable = PageRequest.of(page, size);

        // Step 4: get the payslips of the team (query gives the dto directly)
        return payslipRepository.getPayslipByManagerId(manager.getId(), runId, pageable);
    }

    // totals of one run: how many payslips, how many paid, total gross, deduction and net
    public PayslipStatRespDto getStat(Long runId) {
        // Step 1: check the payroll run id is there in DB
        payrollrunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll Run Not Found"));

        // Step 2: get the payslips of the run and add them up
        List<Payslip> payslips = payslipRepository.findByRunId(runId);
        long paid = payslips.stream().filter(payslip -> payslip.getPayStatus() == PayStatus.PAID).count();
        double gross = payslips.stream().mapToDouble(Payslip::getGross).sum();
        double deduction = payslips.stream().mapToDouble(Payslip::getDeduction).sum();
        double net = payslips.stream().mapToDouble(Payslip::getNet).sum();

        return new PayslipStatRespDto(runId, payslips.size(), paid,
                Math.round(gross * 100.0) / 100.0,
                Math.round(deduction * 100.0) / 100.0,
                Math.round(net * 100.0) / 100.0);
    }

    // payslips of the logged in employee
    public List<PayslipRespDto> getMine(String email, Integer page, Integer size) {
        // Step 1: get the logged in employee from DB using the email of the token
        Employee employee = employeeRepository.getByUserEmail(email);
        if(employee == null)
            throw new ResourceNotFoundException("Employee Not Found");

        // Step 2: create Pageable using page and size values thru PageRequest
        Pageable pageable = PageRequest.of(page, size);

        // Step 3: get the payslips of this employee (query gives the dto directly)
        return payslipRepository.getPayslipByEmployeeId(employee.getId(), pageable);
    }
    // payslips of the logged in employee in one year (version 2 of payslip/my)
    public List<PayslipRespDto> getMineByYear(String email, Integer year, Integer page, Integer size) {
        // Step 1: the year should be a sensible one
        if(year < 2000 || year > 2100)
            throw new InvalidCallException("Year must be between 2000 and 2100");

        // Step 2: get the logged in employee from DB using the email of the token
        Employee employee = employeeRepository.getByUserEmail(email);
        if(employee == null)
            throw new ResourceNotFoundException("Employee Not Found");

        // Step 3: the first day and the last day of the year
        LocalDate fromDate = LocalDate.of(year, 1, 1);
        LocalDate toDate = LocalDate.of(year, 12, 31);

        // Step 4: create Pageable using page and size values thru PageRequest
        Pageable pageable = PageRequest.of(page, size);

        // Step 5: get the payslips of this employee in this year (query gives the dto directly)
        return payslipRepository.getPayslipByEmployeeIdAndYear(employee.getId(), fromDate, toDate, pageable);
    }
}
