package com.example.payroll_management_system.service;

import com.example.payroll_management_system.dto.requestDto.LeaveDto;
import com.example.payroll_management_system.enums.LeaveStatus;
import com.example.payroll_management_system.enums.NotificationType;
import com.example.payroll_management_system.enums.Role;
import com.example.payroll_management_system.exceptions.InvalidCallException;
import com.example.payroll_management_system.exceptions.OwnershipException;
import com.example.payroll_management_system.model.Employee;
import com.example.payroll_management_system.model.LeaveRequest;
import com.example.payroll_management_system.model.User;
import com.example.payroll_management_system.repository.EmployeeRepository;
import com.example.payroll_management_system.repository.LeaveRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeaveServiceTest {

    @InjectMocks
    private LeaveService leaveService;

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditService auditService;

    private Employee getEmployee(Long id, String name, String email, Employee manager){
        Employee employee = new Employee();
        employee.setId(id);
        employee.setFullName(name);
        employee.setUser(new User(email, "hashed", Role.EMPLOYEE));
        employee.setManager(manager);
        return employee;
    }

    private LeaveRequest getLeave(Employee employee){
        LeaveRequest leave = new LeaveRequest();
        leave.setId(10L);
        leave.setEmployee(employee);
        leave.setFromDate(LocalDate.of(2026, 10, 10));
        leave.setToDate(LocalDate.of(2026, 10, 12));
        leave.setType("SICK");
        leave.setStatus(LeaveStatus.PENDING);
        return leave;
    }

    @Test
    public void addTest(){
        // mock
        String email = "ravi@easypay.com";
        LeaveDto leaveDto = new LeaveDto(LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 3), "CASUAL");
        Employee monisha = getEmployee(1L, "Monisha Mahesh", "monisha@easypay.com", null);
        Employee ravi = getEmployee(3L, "Ravi Kumar", email, monisha);

        when(employeeRepository.getByUserEmail(email)).thenReturn(ravi);
        when(leaveRequestRepository.countOverlappingLeaves(3L, LeaveStatus.REJECTED,
                LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 3))).thenReturn(0L);

        // actual call
        leaveService.add(email, leaveDto);

        verify(employeeRepository, times(1)).getByUserEmail(email);
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
        // the manager is told that a new leave is waiting
        verify(notificationService, times(1)).create(any(User.class), anyString(), any(NotificationType.class));
    }

    @Test
    public void addTest_ManagerHasNobodyToApprove(){
        Employee monisha = getEmployee(1L, "Monisha Mahesh", "monisha@easypay.com", null);
        when(employeeRepository.getByUserEmail("monisha@easypay.com")).thenReturn(monisha);

        assertThrows(InvalidCallException.class, ()-> leaveService.add("monisha@easypay.com",
                new LeaveDto(LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 3), "CASUAL")));
        verify(leaveRequestRepository, times(0)).save(any(LeaveRequest.class));
    }

    @Test
    public void addTest_OverlappingDates(){
        Employee monisha = getEmployee(1L, "Monisha Mahesh", "monisha@easypay.com", null);
        Employee ravi = getEmployee(3L, "Ravi Kumar", "ravi@easypay.com", monisha);
        when(employeeRepository.getByUserEmail("ravi@easypay.com")).thenReturn(ravi);
        when(leaveRequestRepository.countOverlappingLeaves(3L, LeaveStatus.REJECTED,
                LocalDate.of(2026, 10, 11), LocalDate.of(2026, 10, 14))).thenReturn(1L);

        assertThrows(InvalidCallException.class, ()-> leaveService.add("ravi@easypay.com",
                new LeaveDto(LocalDate.of(2026, 10, 11), LocalDate.of(2026, 10, 14), "CASUAL")));
        verify(leaveRequestRepository, times(0)).save(any(LeaveRequest.class));
    }

    @Test
    public void reviewTest(){
        // monisha is the manager of ravi
        Employee monisha = getEmployee(1L, "Monisha Mahesh", "monisha@easypay.com", null);
        Employee ravi = getEmployee(3L, "Ravi Kumar", "ravi@easypay.com", monisha);
        LeaveRequest leave = getLeave(ravi);

        when(leaveRequestRepository.findById(10L)).thenReturn(Optional.of(leave));
        when(employeeRepository.getByUserEmail("monisha@easypay.com")).thenReturn(monisha);

        // actual call
        leaveService.review(10L, "monisha@easypay.com", LeaveStatus.APPROVED);

        assertEquals(LeaveStatus.APPROVED, leave.getStatus());
        assertEquals(monisha, leave.getReviewedBy());
        verify(leaveRequestRepository, times(1)).findById(10L);
        verify(leaveRequestRepository, times(1)).save(leave);
        verify(notificationService, times(1)).create(any(User.class), anyString(), any(NotificationType.class));
        verify(auditService, times(1)).log(any(User.class), anyString(), anyString(), anyLong());
    }

    @Test
    public void reviewTest_OwnershipCheck(){
        // priya is the manager of another team, ravi is not in her team
        Employee monisha = getEmployee(1L, "Monisha Mahesh", "monisha@easypay.com", null);
        Employee priya = getEmployee(2L, "Priya Nair", "priya@easypay.com", null);
        Employee ravi = getEmployee(3L, "Ravi Kumar", "ravi@easypay.com", monisha);
        LeaveRequest leave = getLeave(ravi);

        when(leaveRequestRepository.findById(10L)).thenReturn(Optional.of(leave));
        when(employeeRepository.getByUserEmail("priya@easypay.com")).thenReturn(priya);

        assertThrows(OwnershipException.class,
                ()-> leaveService.review(10L, "priya@easypay.com", LeaveStatus.APPROVED));
        assertEquals(LeaveStatus.PENDING, leave.getStatus());
        verify(leaveRequestRepository, times(0)).save(any(LeaveRequest.class));
    }

    @Test
    public void reviewTest_AlreadyReviewed(){
        Employee monisha = getEmployee(1L, "Monisha Mahesh", "monisha@easypay.com", null);
        Employee ravi = getEmployee(3L, "Ravi Kumar", "ravi@easypay.com", monisha);
        LeaveRequest leave = getLeave(ravi);
        leave.setStatus(LeaveStatus.APPROVED);

        when(leaveRequestRepository.findById(10L)).thenReturn(Optional.of(leave));
        when(employeeRepository.getByUserEmail("monisha@easypay.com")).thenReturn(monisha);

        assertThrows(InvalidCallException.class,
                ()-> leaveService.review(10L, "monisha@easypay.com", LeaveStatus.REJECTED));
        verify(leaveRequestRepository, times(0)).save(any(LeaveRequest.class));
    }
    @Test
    public void cancelTest(){
        // ravi takes back his own pending leave
        Employee monisha = getEmployee(1L, "Monisha Mahesh", "monisha@easypay.com", null);
        Employee ravi = getEmployee(3L, "Ravi Kumar", "ravi@easypay.com", monisha);
        LeaveRequest leave = getLeave(ravi);

        when(leaveRequestRepository.findById(10L)).thenReturn(Optional.of(leave));
        when(employeeRepository.getByUserEmail("ravi@easypay.com")).thenReturn(ravi);

        leaveService.cancel(10L, "ravi@easypay.com");

        verify(leaveRequestRepository, times(1)).delete(leave);
        verify(notificationService, times(1)).create(any(User.class), anyString(), any(NotificationType.class));
        verify(auditService, times(1)).log(any(User.class), anyString(), anyString(), anyLong());
    }

    @Test
    public void cancelTest_OwnershipCheck(){
        // anita is logged in, but the leave is of ravi
        Employee monisha = getEmployee(1L, "Monisha Mahesh", "monisha@easypay.com", null);
        Employee ravi = getEmployee(3L, "Ravi Kumar", "ravi@easypay.com", monisha);
        Employee anita = getEmployee(4L, "Anita Rao", "anita@easypay.com", monisha);
        LeaveRequest leave = getLeave(ravi);

        when(leaveRequestRepository.findById(10L)).thenReturn(Optional.of(leave));
        when(employeeRepository.getByUserEmail("anita@easypay.com")).thenReturn(anita);

        assertThrows(OwnershipException.class, ()-> leaveService.cancel(10L, "anita@easypay.com"));
        verify(leaveRequestRepository, times(0)).delete(any(LeaveRequest.class));
    }

    @Test
    public void cancelTest_AlreadyReviewed(){
        // a leave that the manager already decided cannot be cancelled
        Employee monisha = getEmployee(1L, "Monisha Mahesh", "monisha@easypay.com", null);
        Employee ravi = getEmployee(3L, "Ravi Kumar", "ravi@easypay.com", monisha);
        LeaveRequest leave = getLeave(ravi);
        leave.setStatus(LeaveStatus.APPROVED);

        when(leaveRequestRepository.findById(10L)).thenReturn(Optional.of(leave));
        when(employeeRepository.getByUserEmail("ravi@easypay.com")).thenReturn(ravi);

        assertThrows(InvalidCallException.class, ()-> leaveService.cancel(10L, "ravi@easypay.com"));
        verify(leaveRequestRepository, times(0)).delete(any(LeaveRequest.class));
    }
}
