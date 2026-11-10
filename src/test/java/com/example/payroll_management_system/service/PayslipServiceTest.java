package com.example.payroll_management_system.service;

import com.example.payroll_management_system.dto.responseDto.PayslipRespDto;
import com.example.payroll_management_system.dto.responseDto.PayslipDetailRespDto;
import com.example.payroll_management_system.enums.ComponentType;
import com.example.payroll_management_system.enums.PayStatus;
import com.example.payroll_management_system.enums.Role;
import com.example.payroll_management_system.exceptions.InvalidCallException;
import com.example.payroll_management_system.exceptions.OwnershipException;
import com.example.payroll_management_system.model.*;
import com.example.payroll_management_system.repository.*;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PayslipServiceTest {

    @InjectMocks
    private PayslipService payslipService;

    @Mock
    private PayslipRepository payslipRepository;

    @Mock
    private PayrollrunRepository payrollrunRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PayslipItemRepository payslipItemRepository;

    @Mock
    private UserRepository userRepository;

    private Payslip payslip;
    private PayslipItem item;

    @BeforeEach
    public void init() {
        // payslip of ravi
        PayrollRun run = new PayrollRun();
        run.setId(1L);
        run.setPeriodStart(LocalDate.of(2026, 10, 1));
        run.setPeriodEnd(LocalDate.of(2026, 10, 31));

        Employee ravi = new Employee();
        ravi.setId(3L);
        ravi.setFullName("Ravi Kumar");
        ravi.setBankAcc("1111222233");
        ravi.setUser(new User("ravi@easypay.com", "hashed", Role.EMPLOYEE));

        payslip = new Payslip();
        payslip.setId(7L);
        payslip.setRun(run);
        payslip.setEmployee(ravi);
        payslip.setGross(51000.0);
        payslip.setDeduction(7100.0);
        payslip.setNet(43900.0);
        payslip.setPayStatus(PayStatus.PENDING);

        item = new PayslipItem();
        item.setName("Basic");
        item.setType(ComponentType.EARNING);
        item.setAmount(32000.0);
        item.setPayslip(payslip);
    }

    @Test
    public void getByIdTest_Owner() {
        when(payslipRepository.findById(7L)).thenReturn(Optional.of(payslip));
        when(userRepository.loadUserByUsername("ravi@easypay.com", true))
                .thenReturn(new User("ravi@easypay.com", "hashed", Role.EMPLOYEE));
        when(payslipItemRepository.findByPayslipIdOrderByIdAsc(7L)).thenReturn(List.of(item));

        PayslipDetailRespDto dto = payslipService.getById(7L, "ravi@easypay.com");

        assertEquals(43900.0, dto.net(), 0.001);
        assertEquals(1, dto.items().size());
        verify(payslipRepository, times(1)).findById(7L);
        verify(payslipItemRepository, times(1)).findByPayslipIdOrderByIdAsc(7L);
    }

    @Test
    public void getByIdTest_OwnershipCheck() {
        // anita is logged in, but this payslip is of ravi
        when(payslipRepository.findById(7L)).thenReturn(Optional.of(payslip));
        when(userRepository.loadUserByUsername("anita@easypay.com", true))
                .thenReturn(new User("anita@easypay.com", "hashed", Role.EMPLOYEE));

        assertThrows(OwnershipException.class, ()-> payslipService.getById(7L, "anita@easypay.com"));
        verify(payslipItemRepository, times(0)).findByPayslipIdOrderByIdAsc(7L);
    }

    @Test
    public void getByIdTest_Processor() {
        // a processor can open any payslip
        when(payslipRepository.findById(7L)).thenReturn(Optional.of(payslip));
        when(userRepository.loadUserByUsername("processor@easypay.com", true))
                .thenReturn(new User("processor@easypay.com", "hashed", Role.PROCESSOR));
        when(payslipItemRepository.findByPayslipIdOrderByIdAsc(7L)).thenReturn(List.of(item));

        PayslipDetailRespDto dto = payslipService.getById(7L, "processor@easypay.com");

        assertEquals("Ravi Kumar", dto.employeeName());

        verify(payslipRepository, times(1)).findById(7L);
        verify(payslipItemRepository, times(1)).findByPayslipIdOrderByIdAsc(7L);
    }

    @Test
    public void getByIdTest_TeamManager() {
        // monisha is the manager of ravi, so she can open his payslip
        Employee monisha = new Employee();
        monisha.setId(1L);
        monisha.setUser(new User("monisha@easypay.com", "hashed", Role.MANAGER));
        payslip.getEmployee().setManager(monisha);

        when(payslipRepository.findById(7L)).thenReturn(Optional.of(payslip));
        when(userRepository.loadUserByUsername("monisha@easypay.com", true)).thenReturn(monisha.getUser());
        when(payslipItemRepository.findByPayslipIdOrderByIdAsc(7L)).thenReturn(List.of(item));

        PayslipDetailRespDto dto = payslipService.getById(7L, "monisha@easypay.com");

        assertEquals("Ravi Kumar", dto.employeeName());

        verify(payslipRepository, times(1)).findById(7L);
        verify(payslipItemRepository, times(1)).findByPayslipIdOrderByIdAsc(7L);
    }

    @Test
    public void getByIdTest_OtherTeamManager() {
        // priya is the manager of another team, so she cannot open the payslip of ravi
        Employee monisha = new Employee();
        monisha.setId(1L);
        monisha.setUser(new User("monisha@easypay.com", "hashed", Role.MANAGER));
        payslip.getEmployee().setManager(monisha);

        when(payslipRepository.findById(7L)).thenReturn(Optional.of(payslip));
        when(userRepository.loadUserByUsername("priya@easypay.com", true))
                .thenReturn(new User("priya@easypay.com", "hashed", Role.MANAGER));

        assertThrows(OwnershipException.class, ()-> payslipService.getById(7L, "priya@easypay.com"));
        verify(payslipItemRepository, times(0)).findByPayslipIdOrderByIdAsc(7L);
    }

    @Test
    public void getMineByYearTest() {
        // ravi asks for his payslips of 2026
        when(employeeRepository.getByUserEmail("ravi@easypay.com")).thenReturn(payslip.getEmployee());
        when(payslipRepository.getPayslipByEmployeeIdAndYear(3L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), PageRequest.of(0, 20)))
                .thenReturn(List.of());

        List<PayslipRespDto> list = payslipService.getMineByYear("ravi@easypay.com", 2026, 0, 20);

        assertEquals(0, list.size());
        verify(payslipRepository, times(1)).getPayslipByEmployeeIdAndYear(3L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), PageRequest.of(0, 20));
    }

    @Test
    public void getMineByYearTest_InvalidYear() {
        // a year like 1900 is refused before the DB is touched
        assertThrows(InvalidCallException.class, ()-> payslipService.getMineByYear("ravi@easypay.com", 1900, 0, 20));
        verifyNoMoreInteractions(payslipRepository);
    }
}
