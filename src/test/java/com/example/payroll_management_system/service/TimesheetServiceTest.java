package com.example.payroll_management_system.service;

import com.example.payroll_management_system.dto.requestDto.TimesheetDto;
import com.example.payroll_management_system.dto.requestDto.TimesheetUpdateDto;
import com.example.payroll_management_system.dto.responseDto.TimeSheetRespDto;
import com.example.payroll_management_system.enums.EmployeeStatus;
import com.example.payroll_management_system.enums.Role;
import com.example.payroll_management_system.enums.TimesheetStatus;
import com.example.payroll_management_system.exceptions.InvalidCallException;
import com.example.payroll_management_system.exceptions.OwnershipException;
import com.example.payroll_management_system.exceptions.ResourceNotFoundException;
import com.example.payroll_management_system.model.Employee;
import com.example.payroll_management_system.model.Timesheet;
import com.example.payroll_management_system.model.User;
import com.example.payroll_management_system.repository.EmployeeRepository;
import com.example.payroll_management_system.repository.TimesheetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TimesheetServiceTest {

    @InjectMocks
    private TimesheetService timesheetService;

    @Mock
    private TimesheetRepository timesheetRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private Employee getEmployee(Long id, String email){
        Employee employee = new Employee();
        employee.setId(id);
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setUser(new User(email, "hashed", Role.EMPLOYEE));
        return employee;
    }

    @Test
    public void getMineTest(){
        TimeSheetRespDto dto1 = new TimeSheetRespDto(1L, LocalDate.of(2026, 10, 1), 8.0, TimesheetStatus.SUBMITTED, 3L, "Ravi Kumar");
        TimeSheetRespDto dto2 = new TimeSheetRespDto(2L, LocalDate.of(2026, 10, 2), 7.5, TimesheetStatus.SUBMITTED, 3L, "Ravi Kumar");
        TimeSheetRespDto dto3 = new TimeSheetRespDto(3L, LocalDate.of(2026, 10, 1), 8.0, TimesheetStatus.SUBMITTED, 4L, "Anita Rao");

        Employee ravi = getEmployee(3L, "ravi@easypay.com");
        Employee anita = getEmployee(4L, "anita@easypay.com");

        when(employeeRepository.getByUserEmail("ravi@easypay.com")).thenReturn(ravi);
        when(employeeRepository.getByUserEmail("anita@easypay.com")).thenReturn(anita);
        when(timesheetRepository.getTimesheetByEmployeeId(3L, PageRequest.of(0, 5)))
                .thenReturn(List.of(dto1, dto2));
        when(timesheetRepository.getTimesheetByEmployeeId(4L, PageRequest.of(0, 5)))
                .thenReturn(List.of(dto3));

        assertEquals(2, timesheetService.getMine("ravi@easypay.com", 0, 5).size());
        assertEquals(1, timesheetService.getMine("anita@easypay.com", 0, 5).size());

        verify(employeeRepository, times(1)).getByUserEmail("ravi@easypay.com");
        verify(employeeRepository, times(1)).getByUserEmail("anita@easypay.com");
        verify(timesheetRepository, times(1))
                .getTimesheetByEmployeeId(3L, PageRequest.of(0, 5));
        verify(timesheetRepository, times(1))
                .getTimesheetByEmployeeId(4L, PageRequest.of(0, 5));
        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(timesheetRepository);
    }

    @Test
    public void getMineTest_Invalid(){
        // no employee for this email
        when(employeeRepository.getByUserEmail("nobody@easypay.com")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> timesheetService.getMine("nobody@easypay.com", 0, 5));
        verify(employeeRepository, times(1)).getByUserEmail("nobody@easypay.com");
        verify(timesheetRepository, times(0))
                .getTimesheetByEmployeeId(1L, PageRequest.of(0, 5));
        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(timesheetRepository);
    }

    @Test
    public void addTest(){
        /*
         * To call add method of service i need 2 things
         * 1. email of the logged in employee
         * 2. timesheetDto
         * this info comes from postman via controller (the email comes from the token).
         * here , we need to define it ourself
         * */
        // mock
        String email = "ravi@easypay.com";
        TimesheetDto timesheetDto = new TimesheetDto(LocalDate.of(2026, 10, 1), 8.0);

        // Before making actual call, lets mock the repositories in that add method
        when(employeeRepository.getByUserEmail(email)).thenReturn(getEmployee(3L, email));
        when(timesheetRepository.save(any(Timesheet.class))).thenReturn(new Timesheet());

        // actual call
        timesheetService.add(email, timesheetDto);

        // Tip: once u mock - it won't go to db but it still make a call.
        // verify if repo calls are happening only once
        verify(employeeRepository, times(1)).getByUserEmail(email);
        verify(timesheetRepository, times(1)).save(any(Timesheet.class));
    }

    @Test
    public void addTest_RetiredEmployee(){
        Employee ravi = getEmployee(3L, "ravi@easypay.com");
        ravi.setStatus(EmployeeStatus.RETIRED);
        when(employeeRepository.getByUserEmail("ravi@easypay.com")).thenReturn(ravi);

        assertThrows(InvalidCallException.class,
                () -> timesheetService.add("ravi@easypay.com", new TimesheetDto(LocalDate.of(2026, 10, 1), 8.0)));
        verify(timesheetRepository, times(0)).save(any(Timesheet.class));
    }

    @Test
    public void addTest_MoreThan24Hours(){
        when(employeeRepository.getByUserEmail("ravi@easypay.com")).thenReturn(getEmployee(3L, "ravi@easypay.com"));

        assertThrows(InvalidCallException.class,
                () -> timesheetService.add("ravi@easypay.com", new TimesheetDto(LocalDate.of(2026, 10, 1), 25.0)));
        verify(timesheetRepository, times(0)).save(any(Timesheet.class));
    }

    @Test
    public void updateTest(){
        Timesheet timesheet = new Timesheet();
        timesheet.setId(5L);
        timesheet.setEmployee(getEmployee(3L, "ravi@easypay.com"));
        timesheet.setHours(8.0);
        timesheet.setStatus(TimesheetStatus.SUBMITTED);
        when(timesheetRepository.findById(5L)).thenReturn(Optional.of(timesheet));

        timesheetService.update(5L, new TimesheetUpdateDto(7.0), "ravi@easypay.com");

        assertEquals(7.0, timesheet.getHours(), 0.001);
        verify(timesheetRepository, times(1)).findById(5L);
        verify(timesheetRepository, times(1)).save(timesheet);
    }

    @Test
    public void updateTest_OwnershipCheck(){
        // this timesheet belongs to anita, but ravi is logged in
        Timesheet timesheet = new Timesheet();
        timesheet.setId(6L);
        timesheet.setEmployee(getEmployee(4L, "anita@easypay.com"));
        timesheet.setStatus(TimesheetStatus.SUBMITTED);
        when(timesheetRepository.findById(6L)).thenReturn(Optional.of(timesheet));

        assertThrows(OwnershipException.class,
                () -> timesheetService.update(6L, new TimesheetUpdateDto(1.0), "ravi@easypay.com"));
        verify(timesheetRepository, times(0)).save(any(Timesheet.class));
    }
}
