package com.example.payroll_management_system.service;

import com.example.payroll_management_system.dto.responseDto.AdminDashboardRespDto;
import com.example.payroll_management_system.dto.responseDto.ManagerDashboardRespDto;
import com.example.payroll_management_system.dto.responseDto.ProcessorDashboardRespDto;
import com.example.payroll_management_system.enums.EmployeeStatus;
import com.example.payroll_management_system.enums.LeaveStatus;
import com.example.payroll_management_system.enums.Role;
import com.example.payroll_management_system.enums.RunStatus;
import com.example.payroll_management_system.enums.TimesheetStatus;
import com.example.payroll_management_system.exceptions.InvalidCallException;
import com.example.payroll_management_system.exceptions.ResourceNotFoundException;
import com.example.payroll_management_system.model.Employee;
import com.example.payroll_management_system.model.PayrollRun;
import com.example.payroll_management_system.model.User;
import com.example.payroll_management_system.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

    @InjectMocks
    private DashboardService dashboardService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private TimesheetRepository timesheetRepository;

    @Mock
    private PaycomponentRepository paycomponentRepository;

    @Mock
    private PayrollrunRepository payrollrunRepository;

    @Mock
    private PayslipRepository payslipRepository;

    private PayrollRun getRun(Long id, RunStatus status){
        PayrollRun run = new PayrollRun();
        run.setId(id);
        run.setStatus(status);
        return run;
    }

    private Employee getManager(Role role){
        Employee manager = new Employee();
        manager.setId(1L);
        manager.setFullName("Monisha Mahesh");
        manager.setUser(new User("monisha@easypay.com", "hashed", role));
        return manager;
    }

    @Test
    public void getAdminDashboardTest(){
        // mock: the service asks the count for every status, so every status is mocked here
        when(userRepository.count()).thenReturn(6L);
        when(userRepository.countUsersByActive(true)).thenReturn(5L);
        when(employeeRepository.count()).thenReturn(4L);
        when(employeeRepository.countByStatus(EmployeeStatus.ONBOARDING)).thenReturn(1L);
        when(employeeRepository.countByStatus(EmployeeStatus.ACTIVE)).thenReturn(2L);
        when(employeeRepository.countByStatus(EmployeeStatus.RETIRED)).thenReturn(1L);
        when(leaveRequestRepository.countByStatus(LeaveStatus.PENDING)).thenReturn(3L);
        when(payrollrunRepository.countByStatus(RunStatus.DRAFT)).thenReturn(1L);
        when(payrollrunRepository.countByStatus(RunStatus.CALCULATED)).thenReturn(2L);
        when(payrollrunRepository.countByStatus(RunStatus.HR_REVIEW)).thenReturn(3L);
        when(payrollrunRepository.countByStatus(RunStatus.APPROVED)).thenReturn(4L);
        when(payrollrunRepository.countByStatus(RunStatus.PAID)).thenReturn(5L);

        // actual call
        AdminDashboardRespDto dto = dashboardService.getAdminDashboard();

        // every number goes to its own field
        assertEquals(6L, dto.totalUsers());
        assertEquals(5L, dto.activeUsers());
        assertEquals(4L, dto.totalEmployees());
        assertEquals(1L, dto.onboardingEmployees());
        assertEquals(2L, dto.activeEmployees());
        assertEquals(1L, dto.retiredEmployees());
        assertEquals(3L, dto.pendingLeaves());
        assertEquals(1L, dto.runsDraft());
        assertEquals(2L, dto.runsCalculated());
        assertEquals(3L, dto.runsHrReview());
        assertEquals(4L, dto.runsApproved());
        assertEquals(5L, dto.runsPaid());

        verify(userRepository, times(1)).count();
        verify(employeeRepository, times(1)).countByStatus(EmployeeStatus.RETIRED);
        verify(payrollrunRepository, times(1)).countByStatus(RunStatus.PAID);
    }

    @Test
    public void getProcessorDashboardTest(){
        // mock: the latest run is run 2 and it is still in DRAFT
        // the service asks the count for every status, so every status is mocked here
        when(payrollrunRepository.findTopByOrderByIdDesc()).thenReturn(getRun(2L, RunStatus.DRAFT));
        when(employeeRepository.countByStatus(EmployeeStatus.ACTIVE)).thenReturn(4L);
        when(paycomponentRepository.countActive()).thenReturn(4L);
        when(timesheetRepository.countByStatus(TimesheetStatus.SUBMITTED)).thenReturn(2L);
        when(timesheetRepository.countByStatus(TimesheetStatus.LOCKED)).thenReturn(1L);
        when(payrollrunRepository.countByStatus(RunStatus.DRAFT)).thenReturn(1L);
        when(payrollrunRepository.countByStatus(RunStatus.CALCULATED)).thenReturn(2L);
        when(payrollrunRepository.countByStatus(RunStatus.HR_REVIEW)).thenReturn(3L);
        when(payrollrunRepository.countByStatus(RunStatus.APPROVED)).thenReturn(4L);
        when(payrollrunRepository.countByStatus(RunStatus.PAID)).thenReturn(5L);

        // actual call
        ProcessorDashboardRespDto dto = dashboardService.getProcessorDashboard();

        // every number goes to its own field
        assertEquals(4L, dto.activeEmployees());
        assertEquals(4L, dto.activePayComponents());
        assertEquals(2L, dto.submittedTimesheets());
        assertEquals(1L, dto.lockedTimesheets());
        assertEquals(1L, dto.runsDraft());
        assertEquals(2L, dto.runsCalculated());
        assertEquals(3L, dto.runsHrReview());
        assertEquals(4L, dto.runsApproved());
        assertEquals(5L, dto.runsPaid());
        assertEquals(Long.valueOf(2L), dto.latestRunId());
        assertEquals(RunStatus.DRAFT, dto.latestRunStatus());

        verify(payrollrunRepository, times(1)).findTopByOrderByIdDesc();
        verify(payrollrunRepository, times(1)).countByStatus(RunStatus.PAID);
    }

    @Test
    public void getProcessorDashboardTest_NoRunYet(){
        // there is no payroll run in DB, so the latest run is empty
        when(payrollrunRepository.findTopByOrderByIdDesc()).thenReturn(null);

        ProcessorDashboardRespDto dto = dashboardService.getProcessorDashboard();

        assertNull(dto.latestRunId());
        assertNull(dto.latestRunStatus());

        verify(payrollrunRepository, times(1)).findTopByOrderByIdDesc();
    }

    @Test
    public void getManagerDashboardTest(){
        // mock: monisha is a manager with one active team member and one pending leave
        when(employeeRepository.getByUserEmail("monisha@easypay.com")).thenReturn(getManager(Role.MANAGER));
        when(payrollrunRepository.findTopByOrderByIdDesc()).thenReturn(getRun(1L, RunStatus.PAID));
        when(payslipRepository.sumTeamNetByRun(1L, 1L)).thenReturn(43900.0);
        when(employeeRepository.countTeam(1L)).thenReturn(1L);
        when(employeeRepository.countTeamByStatus(1L, EmployeeStatus.ACTIVE)).thenReturn(1L);
        when(leaveRequestRepository.countTeamLeavesByStatus(1L, LeaveStatus.PENDING)).thenReturn(1L);

        // actual call
        ManagerDashboardRespDto dto = dashboardService.getManagerDashboard("monisha@easypay.com");

        assertEquals("Monisha Mahesh", dto.managerName());
        assertEquals(1L, dto.teamSize());
        assertEquals(1L, dto.activeTeamMembers());
        assertEquals(1L, dto.pendingLeaves());
        assertEquals(Long.valueOf(1L), dto.latestRunId());
        assertEquals(RunStatus.PAID, dto.latestRunStatus());
        assertEquals(43900.0, dto.latestRunTeamNetPay(), 0.001);

        verify(employeeRepository, times(1)).getByUserEmail("monisha@easypay.com");
        verify(payslipRepository, times(1)).sumTeamNetByRun(1L, 1L);
    }

    @Test
    public void getManagerDashboardTest_NoRunYet(){
        // no run, so the team net pay is 0 and the payslips are not even asked
        when(employeeRepository.getByUserEmail("monisha@easypay.com")).thenReturn(getManager(Role.MANAGER));
        when(payrollrunRepository.findTopByOrderByIdDesc()).thenReturn(null);

        ManagerDashboardRespDto dto = dashboardService.getManagerDashboard("monisha@easypay.com");

        assertNull(dto.latestRunId());
        assertEquals(0.0, dto.latestRunTeamNetPay(), 0.001);
        verify(employeeRepository, times(1)).getByUserEmail("monisha@easypay.com");
        verifyNoMoreInteractions(payslipRepository);
    }

    @Test
    public void getManagerDashboardTest_NoPayslipYet(){
        // the run is there, but the team has no payslip in it, so the sum is null
        when(employeeRepository.getByUserEmail("monisha@easypay.com")).thenReturn(getManager(Role.MANAGER));
        when(payrollrunRepository.findTopByOrderByIdDesc()).thenReturn(getRun(2L, RunStatus.DRAFT));
        when(payslipRepository.sumTeamNetByRun(1L, 2L)).thenReturn(null);

        ManagerDashboardRespDto dto = dashboardService.getManagerDashboard("monisha@easypay.com");

        assertEquals(0.0, dto.latestRunTeamNetPay(), 0.001);

        verify(payslipRepository, times(1)).sumTeamNetByRun(1L, 2L);
    }

    @Test
    public void getManagerDashboardTest_ManagerNotFound(){
        when(employeeRepository.getByUserEmail("nobody@easypay.com")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, ()-> dashboardService.getManagerDashboard("nobody@easypay.com"));

        verify(employeeRepository, times(1)).getByUserEmail("nobody@easypay.com");
        verify(payrollrunRepository, times(0)).findTopByOrderByIdDesc();
    }

    @Test
    public void getManagerDashboardTest_NotAManager(){
        // an employee asks for the manager dashboard
        when(employeeRepository.getByUserEmail("ravi@easypay.com")).thenReturn(getManager(Role.EMPLOYEE));

        assertThrows(InvalidCallException.class, ()-> dashboardService.getManagerDashboard("ravi@easypay.com"));

        verify(employeeRepository, times(1)).getByUserEmail("ravi@easypay.com");
        verify(payrollrunRepository, times(0)).findTopByOrderByIdDesc();
    }
}
