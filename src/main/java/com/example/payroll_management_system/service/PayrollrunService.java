package com.example.payroll_management_system.service;

import com.example.payroll_management_system.dto.requestDto.PayrollrunDto;
import com.example.payroll_management_system.dto.requestDto.RemarksDto;
import com.example.payroll_management_system.dto.responseDto.PayrollVerifyRespDto;
import com.example.payroll_management_system.dto.responseDto.PayrollrunRespDto;
import com.example.payroll_management_system.enums.ComponentType;
import com.example.payroll_management_system.enums.EmployeeStatus;
import com.example.payroll_management_system.enums.NotificationType;
import com.example.payroll_management_system.enums.PayStatus;
import com.example.payroll_management_system.enums.Role;
import com.example.payroll_management_system.enums.RunStatus;
import com.example.payroll_management_system.exceptions.InvalidCallException;
import com.example.payroll_management_system.exceptions.ResourceNotFoundException;
import com.example.payroll_management_system.mapper.PayrollrunMapper;
import com.example.payroll_management_system.model.Employee;
import com.example.payroll_management_system.model.PayComponent;
import com.example.payroll_management_system.model.PayrollRun;
import com.example.payroll_management_system.model.Payslip;
import com.example.payroll_management_system.model.PayslipItem;
import com.example.payroll_management_system.model.User;
import com.example.payroll_management_system.repository.EmployeeRepository;
import com.example.payroll_management_system.repository.PaycomponentRepository;
import com.example.payroll_management_system.repository.PayrollrunRepository;
import com.example.payroll_management_system.repository.PayslipItemRepository;
import com.example.payroll_management_system.repository.PayslipRepository;
import com.example.payroll_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollrunService {

    private final PayrollrunRepository payrollrunRepository;
    private final PayslipRepository payslipRepository;
    private final PayslipItemRepository payslipItemRepository;
    private final EmployeeRepository employeeRepository;
    private final PaycomponentRepository paycomponentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public void add(PayrollrunDto payrollrunDto) {
        // Step 1: period end should not be before period start
        if(payrollrunDto.periodEnd().isBefore(payrollrunDto.periodStart()))
            throw new InvalidCallException("Period end cannot be before period start");

        // Step 1.5: another run should not be there for the same or overlapping period
        long count = payrollrunRepository.countOverlappingRuns(
                payrollrunDto.periodStart(), payrollrunDto.periodEnd());
        if(count > 0)
            throw new InvalidCallException("A payroll run already exists for this period");

        // Step 2: change PayrollrunDto to Payrollrun entity using mapper
        PayrollRun run = PayrollrunMapper.convertDtoToEntity(payrollrunDto);

        // Step 2.5: new run starts with DRAFT status
        run.setStatus(RunStatus.DRAFT);

        // Step 3: save run in DB
        payrollrunRepository.save(run);
        log.info("Payroll run created for {} to {}", payrollrunDto.periodStart(), payrollrunDto.periodEnd());
    }

    public List<PayrollrunRespDto> getAll(Integer page, Integer size) {
        // Step 1: create Pageable using page and size values thru PageRequest
        Pageable pageable = PageRequest.of(page, size);

        // Step 2: get the payroll runs of that page from DB
        List<PayrollRun> list = payrollrunRepository.findAll(pageable).getContent();

        // Step 3: Map List<Payrollrun> to List<PayrollrunRespDto> using Mapper
        return list
                .stream()
                .map(PayrollrunMapper :: convertEntityToDto)
                .toList();
    }

    public PayrollrunRespDto getById(Long id) {
        // Step 1: get the payroll run from DB using the id given in url
        PayrollRun run = payrollrunRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Payroll Run Not Found"));

        // Step 2: change Payrollrun entity to PayrollrunRespDto using mapper
        return PayrollrunMapper.convertEntityToDto(run);
    }

    public List<PayrollrunRespDto> getByStatus(RunStatus status, Integer page, Integer size) {
        // Step 1: create Pageable using page and size values thru PageRequest
        Pageable pageable = PageRequest.of(page, size);

        // Step 2: get the payroll runs with this status from DB
        List<PayrollRun> list = payrollrunRepository.findByStatus(status, pageable);

        // Step 3: Map List<PayrollRun> to List<PayrollrunRespDto> using Mapper
        return list
                .stream()
                .map(PayrollrunMapper :: convertEntityToDto)
                .toList();
    }

    // processor calculates the payroll. one payslip (with its item lines) is made for every employee
    @Transactional
    public void calculate(Long runId, String email) {
        // Step 1: get the run from DB using the id given in url
        PayrollRun run = getRun(runId);

        // Step 2: get the processor and check the role
        User processor = getUserWithRole(email, Role.PROCESSOR, "Only a processor can calculate payroll");

        // Step 3: only a DRAFT run can be calculated
        if(run.getStatus() != RunStatus.DRAFT)
            throw new InvalidCallException("Only a DRAFT run can be calculated");

        // Step 4: payslip is made only for ACTIVE employees who joined on or before the period end
        List<Employee> employees = employeeRepository
                .findByStatusAndJoinDateLessThanEqual(EmployeeStatus.ACTIVE, run.getPeriodEnd());
        if(employees.isEmpty())
            throw new InvalidCallException("No active employees found for this period");

        // Step 5: pay components that are active (retired ones are skipped)
        List<PayComponent> earnings = paycomponentRepository.getActiveByType(ComponentType.EARNING);
        List<PayComponent> deductions = paycomponentRepository.getActiveByType(ComponentType.DEDUCTION);
        List<PayComponent> benefits = paycomponentRepository.getActiveByType(ComponentType.BENEFIT);

        // Step 6: make the payslip and its items for every employee
        for(Employee employee : employees){
            List<PayslipItem> items = new ArrayList<>();

            // gross = basic + hra + allowance + earning components
            double gross = 0;
            gross += addItem(items, "Basic", ComponentType.EARNING, employee.getBasic(), false);
            gross += addItem(items, "HRA", ComponentType.EARNING, employee.getHra(), false);
            gross += addItem(items, "Allowance", ComponentType.EARNING, employee.getAllowance(), false);
            for(PayComponent component : earnings)
                gross += addItem(items, component.getName(), ComponentType.EARNING, component.getValue(), component.isStatutory());

            // deduction = deduction components + tax withholding of the employee (percent of gross)
            double deduction = 0;
            for(PayComponent component : deductions)
                deduction += addItem(items, component.getName(), ComponentType.DEDUCTION, component.getValue(), component.isStatutory());
            double taxPercent = employee.getTaxWithholding() == null ? 0 : employee.getTaxWithholding();
            if(taxPercent > 0)
                deduction += addItem(items, "Tax withholding", ComponentType.DEDUCTION, gross * taxPercent / 100, true);

            // benefits are shown on the pay stub, but they are not part of gross, deduction or net
            for(PayComponent component : benefits)
                addItem(items, component.getName(), ComponentType.BENEFIT, component.getValue(), component.isStatutory());

            // save the payslip first, then its items (items need the payslip id)
            Payslip payslip = new Payslip();
            payslip.setRun(run);
            payslip.setEmployee(employee);
            payslip.setGross(round(gross));
            payslip.setDeduction(round(deduction));
            payslip.setNet(round(gross - deduction));
            payslip.setPayStatus(PayStatus.PENDING);
            Payslip savedPayslip = payslipRepository.save(payslip);

            items.forEach(item -> item.setPayslip(savedPayslip));
            payslipItemRepository.saveAll(items);
            log.info("Payroll run {} is calculated by {}", runId, email);
        }

        // Step 7: update the run
        run.setStatus(RunStatus.CALCULATED);
        run.setProcessedBy(processor);
        run.setRemarks(null);
        payrollrunRepository.save(run);

        // Step 8: audit entry (done by the processor)
        auditService.log(processor, "PAYROLL_CALCULATED", "PAYROLL_RUN", runId);
    }

    // processor sends the calculated run to HR (admin) for review
    @Transactional
    public void submit(Long runId, String email) {
        // Step 1: get the run and the processor
        PayrollRun run = getRun(runId);
        User processor = getUserWithRole(email, Role.PROCESSOR, "Only a processor can submit payroll");

        // Step 2: only a CALCULATED run can be submitted
        if(run.getStatus() != RunStatus.CALCULATED)
            throw new InvalidCallException("Only a CALCULATED run can be submitted for review");

        // Step 3: a run with issues cannot go to HR. correct the data and reprocess it
        List<String> issues = findIssues(payslipRepository.findByRunId(runId));
        if(!issues.isEmpty())
            throw new InvalidCallException("Run has " + issues.size() + " issue(s). Check the verify api, correct the data and reprocess");

        // Step 4: update the run
        run.setStatus(RunStatus.HR_REVIEW);
        payrollrunRepository.save(run);

        // Step 5: tell every active admin that the run is waiting for approval, and keep an audit entry
        for(User admin : userRepository.findByRole(Role.ADMIN)){
            if(admin.isActive())
                notificationService.create(admin, "Payroll run " + runId + " is waiting for your approval", NotificationType.PAYROLL);
        }
        auditService.log(processor, "PAYROLL_SUBMITTED", "PAYROLL_RUN", runId);
        log.info("Payroll run {} is sent to HR by {}", runId, email);
    }

    // admin (HR) approves the run
    @Transactional
    public void approve(Long runId, String email) {
        // Step 1: get the run and the admin
        PayrollRun run = getRun(runId);
        User admin = getUserWithRole(email, Role.ADMIN, "Only an admin can approve payroll");

        // Step 2: only a run in HR_REVIEW can be approved
        if(run.getStatus() != RunStatus.HR_REVIEW)
            throw new InvalidCallException("Only a run in HR_REVIEW can be approved");

        // Step 3: update the run
        run.setStatus(RunStatus.APPROVED);
        run.setApprovedBy(admin);
        payrollrunRepository.save(run);

        // Step 4: tell the processor to pay, and keep an audit entry
        if(run.getProcessedBy() != null)
            notificationService.create(run.getProcessedBy(),
                    "Payroll run " + runId + " is approved. You can process the payment now", NotificationType.PAYROLL);
        auditService.log(admin, "PAYROLL_APPROVED", "PAYROLL_RUN", runId);
        log.info("Payroll run {} is approved by {}", runId, email);
    }

    // processor pays the approved run. every payslip becomes PAID
    @Transactional
    public void pay(Long runId, String email) {
        // Step 1: get the run and the processor
        PayrollRun run = getRun(runId);
        User processor = getUserWithRole(email, Role.PROCESSOR, "Only a processor can pay payroll");

        // Step 2: only an APPROVED run can be paid
        if(run.getStatus() != RunStatus.APPROVED)
            throw new InvalidCallException("Only an APPROVED run can be paid");

        // Step 3: mark every payslip of the run as paid
        List<Payslip> payslips = payslipRepository.findByRunId(runId);
        payslips.forEach(payslip -> {
            payslip.setPayStatus(PayStatus.PAID);
            payslip.setPaidOn(LocalDate.now());
        });
        payslipRepository.saveAll(payslips);

        // Step 4: update the run
        run.setStatus(RunStatus.PAID);
        payrollrunRepository.save(run);

        // Step 5: payday notification for every employee, and keep an audit entry
        for(Payslip payslip : payslips){
            notificationService.create(payslip.getEmployee().getUser(),
                    "Salary for " + run.getPeriodStart() + " to " + run.getPeriodEnd() + " is paid. Net pay: " + payslip.getNet(),
                    NotificationType.PAYDAY);
        }
        auditService.log(processor, "PAYROLL_PAID", "PAYROLL_RUN", runId);

        log.info("Payroll run {} is paid by {}", runId, email);
    }

    // data was wrong, so the run goes back to DRAFT. old payslips are deleted and the reason is saved in remarks
    @Transactional
    public void reprocess(Long runId, String email, RemarksDto remarksDto) {
        // Step 1: get the run and the user
        PayrollRun run = getRun(runId);
        User user = userRepository.loadUserByUsername(email, true);
        if(user == null)
            throw new ResourceNotFoundException("User Not Found");

        // Step 2: only a processor or an admin can reprocess
        if(user.getRole() != Role.PROCESSOR && user.getRole() != Role.ADMIN)
            throw new InvalidCallException("Only a processor or an admin can reprocess payroll");

        // Step 3: an approved or paid run cannot go back
        if(run.getStatus() != RunStatus.CALCULATED && run.getStatus() != RunStatus.HR_REVIEW)
            throw new InvalidCallException("Only a CALCULATED or HR_REVIEW run can be reprocessed");

        // Step 4: delete the payslip items first and then the payslips
        payslipItemRepository.deleteItemsByRunId(runId);
        payslipRepository.deletePayslipsByRunId(runId);

        // Step 5: back to DRAFT with the reason
        User processedBy = run.getProcessedBy();
        run.setStatus(RunStatus.DRAFT);
        run.setProcessedBy(null);
        run.setApprovedBy(null);
        run.setRemarks(remarksDto.remarks());
        payrollrunRepository.save(run);

        // Step 6: tell the processor if someone else sent the run back, and keep an audit entry
        if(processedBy != null && !processedBy.getId().equals(user.getId()))
            notificationService.create(processedBy,
                    "Payroll run " + runId + " is sent back for reprocess: " + remarksDto.remarks(), NotificationType.PAYROLL);
        auditService.log(user, "PAYROLL_REPROCESS", "PAYROLL_RUN", runId);

        log.info("Payroll run {} is sent back by {}", runId, email);
    }

    // processor checks the calculated payroll before sending it to HR
    public PayrollVerifyRespDto verify(Long runId) {
        // Step 1: get the run from DB using the id given in url
        PayrollRun run = getRun(runId);

        // Step 2: a DRAFT run has no payslips yet
        if(run.getStatus() == RunStatus.DRAFT)
            throw new InvalidCallException("Run is not calculated yet. Calculate it first");

        // Step 3: find the issues in the payslips of this run
        List<Payslip> payslips = payslipRepository.findByRunId(runId);
        List<String> issues = findIssues(payslips);

        return new PayrollVerifyRespDto(runId, run.getStatus(), payslips.size(), issues.size(), issues);
    }

    private PayrollRun getRun(Long runId) {
        return payrollrunRepository.findById(runId)
                .orElseThrow(()-> new ResourceNotFoundException("Payroll Run Not Found"));
    }

    // the user is the logged in user (found by the email of the token). his role should match the work
    private User getUserWithRole(String email, Role role, String message) {
        User user = userRepository.loadUserByUsername(email, true);
        if(user == null)
            throw new ResourceNotFoundException("User Not Found");
        if(user.getRole() != role)
            throw new InvalidCallException(message);
        return user;
    }

    // bank account is missing or net pay is zero or less
    private List<String> findIssues(List<Payslip> payslips) {
        List<String> issues = new ArrayList<>();
        if(payslips.isEmpty())
            issues.add("No payslips found in this run");
        for(Payslip payslip : payslips){
            Employee employee = payslip.getEmployee();
            if(employee.getBankAcc() == null || employee.getBankAcc().trim().isEmpty())
                issues.add(employee.getFullName() + " (id " + employee.getId() + ") has no bank account");
            if(payslip.getNet() <= 0)
                issues.add(employee.getFullName() + " (id " + employee.getId() + ") has net pay " + payslip.getNet());
        }
        return issues;
    }

    // adds one item line to the list and gives back the rounded amount
    private double addItem(List<PayslipItem> items, String name, ComponentType type, double amount, boolean statutory) {
        PayslipItem item = new PayslipItem();
        item.setName(name);
        item.setType(type);
        item.setAmount(round(amount));
        item.setStatutory(statutory);
        items.add(item);
        return item.getAmount();
    }

    // money is kept to 2 decimal places
    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
