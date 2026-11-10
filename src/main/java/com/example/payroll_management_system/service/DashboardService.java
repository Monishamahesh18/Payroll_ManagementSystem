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
import com.example.payroll_management_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final TimesheetRepository timesheetRepository;
    private final PaycomponentRepository paycomponentRepository;
    private final PayrollrunRepository payrollrunRepository;
    private final PayslipRepository payslipRepository;

    public AdminDashboardRespDto getAdminDashboard() {
        // all the numbers are counts from DB, so nothing is saved here
        return new AdminDashboardRespDto(
                userRepository.count(),
                userRepository.countUsersByActive(true),
                employeeRepository.count(),
                employeeRepository.countByStatus(EmployeeStatus.ONBOARDING),
                employeeRepository.countByStatus(EmployeeStatus.ACTIVE),
                employeeRepository.countByStatus(EmployeeStatus.RETIRED),
                leaveRequestRepository.countByStatus(LeaveStatus.PENDING),
                payrollrunRepository.countByStatus(RunStatus.DRAFT),
                payrollrunRepository.countByStatus(RunStatus.CALCULATED),
                payrollrunRepository.countByStatus(RunStatus.HR_REVIEW),
                payrollrunRepository.countByStatus(RunStatus.APPROVED),
                payrollrunRepository.countByStatus(RunStatus.PAID)
        );
    }

    public ProcessorDashboardRespDto getProcessorDashboard() {
        // latest run is null when there is no run yet
        PayrollRun latestRun = payrollrunRepository.findTopByOrderByIdDesc();

        return new ProcessorDashboardRespDto(
                employeeRepository.countByStatus(EmployeeStatus.ACTIVE),
                paycomponentRepository.countActive(),
                timesheetRepository.countByStatus(TimesheetStatus.SUBMITTED),
                timesheetRepository.countByStatus(TimesheetStatus.LOCKED),
                payrollrunRepository.countByStatus(RunStatus.DRAFT),
                payrollrunRepository.countByStatus(RunStatus.CALCULATED),
                payrollrunRepository.countByStatus(RunStatus.HR_REVIEW),
                payrollrunRepository.countByStatus(RunStatus.APPROVED),
                payrollrunRepository.countByStatus(RunStatus.PAID),
                latestRun == null ? null : latestRun.getId(),
                latestRun == null ? null : latestRun.getStatus()
        );
    }

    public ManagerDashboardRespDto getManagerDashboard(String email) {
        // Step 1: get the logged in manager from DB using the email of the token
        Employee manager = employeeRepository.getByUserEmail(email);
        if(manager == null)
            throw new ResourceNotFoundException("Manager Id Invalid");
        Long managerId = manager.getId();

        // Step 2: the id should belong to a manager only
        if(manager.getUser().getRole() != Role.MANAGER)
            throw new InvalidCallException("Given id does not belong to a manager");

        // Step 3: team payroll of the latest run (0 if there is no run or no payslip yet)
        PayrollRun latestRun = payrollrunRepository.findTopByOrderByIdDesc();
        double teamNet = 0;
        if(latestRun != null){
            Double sum = payslipRepository.sumTeamNetByRun(managerId, latestRun.getId());
            teamNet = sum == null ? 0 : sum;
        }

        // Step 4: counts for the team
        return new ManagerDashboardRespDto(
                managerId,
                manager.getFullName(),
                employeeRepository.countTeam(managerId),
                employeeRepository.countTeamByStatus(managerId, EmployeeStatus.ACTIVE),
                leaveRequestRepository.countTeamLeavesByStatus(managerId, LeaveStatus.PENDING),
                latestRun == null ? null : latestRun.getId(),
                latestRun == null ? null : latestRun.getStatus(),
                teamNet
        );
    }
}
