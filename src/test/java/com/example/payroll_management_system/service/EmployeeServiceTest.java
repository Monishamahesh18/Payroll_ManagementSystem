package com.example.payroll_management_system.service;

import com.example.payroll_management_system.dto.responseDto.EmployeeRespDto;
import com.example.payroll_management_system.enums.EmployeeStatus;
import com.example.payroll_management_system.enums.Role;
import com.example.payroll_management_system.exceptions.ResourceNotFoundException;
import com.example.payroll_management_system.model.Employee;
import com.example.payroll_management_system.model.User;
import com.example.payroll_management_system.repository.EmployeeRepository;
import com.example.payroll_management_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @InjectMocks
    private EmployeeService employeeService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    private Employee getEmployee(Long id, String email, Employee manager){
        Employee employee = new Employee();
        employee.setId(id);
        employee.setEmpCode("E001");
        employee.setFullName("Ravi Kumar");
        employee.setDepartment("Finance");
        employee.setJoinDate(LocalDate.of(2026, 1, 5));
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setBasic(32000.0);
        employee.setHra(12000.0);
        employee.setAllowance(5000.0);
        employee.setManager(manager);
        employee.setUser(new User(email, "hashed", Role.EMPLOYEE));
        return employee;
    }

    @Test
    public void getMineTest(){
        // the logged in employee asks for his own details
        Employee manager = getEmployee(1L, "monisha@easypay.com", null);
        manager.setFullName("Monisha Mahesh");
        Employee employee = getEmployee(3L, "ravi@easypay.com", manager);
        when(employeeRepository.getByUserEmail("ravi@easypay.com")).thenReturn(employee);

        // actual call
        EmployeeRespDto dto = employeeService.getMine("ravi@easypay.com");

        assertEquals(Long.valueOf(3L), dto.employeeId());
        assertEquals("Ravi Kumar", dto.fullName());
        assertEquals("ravi@easypay.com", dto.email());
        assertEquals(Role.EMPLOYEE, dto.role());
        assertEquals(Double.valueOf(32000.0), dto.basic());
        assertEquals("Monisha Mahesh", dto.managerName());

        verify(employeeRepository, times(1)).getByUserEmail("ravi@easypay.com");
    }

    @Test
    public void getMineWithoutManagerTest(){
        // a top manager has no manager above him, so the manager name is null
        Employee employee = getEmployee(1L, "monisha@easypay.com", null);
        when(employeeRepository.getByUserEmail("monisha@easypay.com")).thenReturn(employee);

        // actual call
        EmployeeRespDto dto = employeeService.getMine("monisha@easypay.com");

        assertNull(dto.managerId());
        assertNull(dto.managerName());

        verify(employeeRepository, times(1)).getByUserEmail("monisha@easypay.com");
    }

    @Test
    public void getMineNotFoundTest(){
        // an admin or a processor has no employee record
        when(employeeRepository.getByUserEmail("admin@easypay.com")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, ()-> employeeService.getMine("admin@easypay.com"));

        verify(employeeRepository, times(1)).getByUserEmail("admin@easypay.com");
    }
}