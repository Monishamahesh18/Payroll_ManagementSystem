package com.example.payroll_management_system.service;

import com.example.payroll_management_system.enums.*;
import com.example.payroll_management_system.exceptions.InvalidCallException;
import com.example.payroll_management_system.model.*;
import com.example.payroll_management_system.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PayrollrunServiceTest {

    @InjectMocks
    private PayrollrunService payrollrunService;

    @Mock
    private PayrollrunRepository payrollrunRepository;

    @Mock
    private PayslipRepository payslipRepository;

    @Mock
    private PayslipItemRepository payslipItemRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PaycomponentRepository paycomponentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditService auditService;

    private PayrollRun run;
    private User processor;
    private Employee ravi;

    private PayComponent getComponent(String name, ComponentType type, double value, boolean statutory){
        PayComponent component = new PayComponent();
        component.setName(name);
        component.setType(type);
        component.setValue(value);
        component.setStatutory(statutory);
        component.setActive(true);
        return component;
    }

    @BeforeEach
    public void init(){
        processor = new User("processor@easypay.com", "hashed", Role.PROCESSOR);
        processor.setActive(true);

        run = new PayrollRun();
        run.setId(1L);
        run.setPeriodStart(LocalDate.of(2026, 10, 1));
        run.setPeriodEnd(LocalDate.of(2026, 10, 31));
        run.setStatus(RunStatus.DRAFT);

        ravi = new Employee();
        ravi.setId(3L);
        ravi.setFullName("Ravi Kumar");
        ravi.setBasic(32000.0);
        ravi.setHra(12000.0);
        ravi.setAllowance(5000.0);
        ravi.setBankAcc("1111222233");
        ravi.setTaxWithholding(10.0);
        ravi.setStatus(EmployeeStatus.ACTIVE);
        ravi.setJoinDate(LocalDate.of(2026, 1, 5));
    }

    @Test
    public void calculateTest(){
        // mock
        when(payrollrunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(userRepository.loadUserByUsername("processor@easypay.com", true)).thenReturn(processor);
        when(employeeRepository.findByStatusAndJoinDateLessThanEqual(EmployeeStatus.ACTIVE, LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(ravi));
        when(paycomponentRepository.getActiveByType(ComponentType.EARNING))
                .thenReturn(List.of(getComponent("Bonus", ComponentType.EARNING, 2000.0, false)));
        when(paycomponentRepository.getActiveByType(ComponentType.DEDUCTION))
                .thenReturn(List.of(getComponent("PF", ComponentType.DEDUCTION, 2000.0, true)));
        when(paycomponentRepository.getActiveByType(ComponentType.BENEFIT))
                .thenReturn(List.of(getComponent("Insurance", ComponentType.BENEFIT, 600.0, false)));
        when(payslipRepository.save(any(Payslip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // actual call
        payrollrunService.calculate(1L, "processor@easypay.com");

        // gross = 32000 + 12000 + 5000 + bonus 2000 = 51000
        // deduction = PF 2000 + tax withholding (10 percent of 51000) 5100 = 7100
        // net = 43900. the benefit is only shown on the pay stub
        ArgumentCaptor<Payslip> captor = ArgumentCaptor.forClass(Payslip.class);
        verify(payslipRepository, times(1)).save(captor.capture());
        assertEquals(51000.0, captor.getValue().getGross(), 0.001);
        assertEquals(7100.0, captor.getValue().getDeduction(), 0.001);
        assertEquals(43900.0, captor.getValue().getNet(), 0.001);
        assertEquals(PayStatus.PENDING, captor.getValue().getPayStatus());
        verify(payslipItemRepository, times(1)).saveAll(anyList());
        assertEquals(RunStatus.CALCULATED, run.getStatus());
        assertEquals(processor, run.getProcessedBy());
        verify(auditService, times(1)).log(any(User.class), anyString(), anyString(), anyLong());
    }

    @Test
    public void calculateTest_AdminCannotCalculate(){
        User admin = new User("admin@easypay.com", "hashed", Role.ADMIN);
        when(payrollrunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(userRepository.loadUserByUsername("admin@easypay.com", true)).thenReturn(admin);

        assertThrows(InvalidCallException.class, ()-> payrollrunService.calculate(1L, "admin@easypay.com"));
        verify(payslipRepository, times(0)).save(any(Payslip.class));
    }

    @Test
    public void calculateTest_RunIsNotDraft(){
        run.setStatus(RunStatus.CALCULATED);
        when(payrollrunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(userRepository.loadUserByUsername("processor@easypay.com", true)).thenReturn(processor);

        assertThrows(InvalidCallException.class, ()-> payrollrunService.calculate(1L, "processor@easypay.com"));
        verify(payslipRepository, times(0)).save(any(Payslip.class));
    }

    @Test
    public void submitTest_EmployeeWithoutBankAccount(){
        // verify finds an issue, so the run cannot go to HR
        run.setStatus(RunStatus.CALCULATED);
        ravi.setBankAcc(null);
        Payslip payslip = new Payslip();
        payslip.setEmployee(ravi);
        payslip.setNet(43900.0);
        when(payrollrunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(userRepository.loadUserByUsername("processor@easypay.com", true)).thenReturn(processor);
        when(payslipRepository.findByRunId(1L)).thenReturn(List.of(payslip));

        assertThrows(InvalidCallException.class, ()-> payrollrunService.submit(1L, "processor@easypay.com"));
        assertEquals(RunStatus.CALCULATED, run.getStatus());
        verify(payrollrunRepository, times(0)).save(any(PayrollRun.class));
    }
}
