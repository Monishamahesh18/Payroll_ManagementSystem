package com.example.payroll_management_system.service;

import com.example.payroll_management_system.dto.requestDto.AdminDto;
import com.example.payroll_management_system.dto.responseDto.DetailsDto;
import com.example.payroll_management_system.enums.Role;
import com.example.payroll_management_system.exceptions.InvalidCallException;
import com.example.payroll_management_system.model.Employee;
import com.example.payroll_management_system.model.User;
import com.example.payroll_management_system.repository.EmployeeRepository;
import com.example.payroll_management_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User getUser(String email, Role role){
        User user = new User(email, "hashed", role);
        user.setActive(true);
        return user;
    }

    @Test
    public void addAdminTest(){
        // mock
        AdminDto adminDto = new AdminDto("admin@easypay.com", "admin123");
        when(passwordEncoder.encode("admin123")).thenReturn("HASHED");

        // actual call
        userService.addAdmin(adminDto);

        // the password that goes to DB is the encoded one
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());
        assertEquals("HASHED", captor.getValue().getPassword());
        assertEquals(Role.ADMIN, captor.getValue().getRole());
        verify(passwordEncoder, times(1)).encode("admin123");
    }

    @Test
    public void addProcessorTest(){
        when(passwordEncoder.encode("proc123")).thenReturn("HASHED");

        userService.addProcessor(new AdminDto("processor@easypay.com", "proc123"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());
        assertEquals("HASHED", captor.getValue().getPassword());
        assertEquals(Role.PROCESSOR, captor.getValue().getRole());
    }

    @Test
    public void getUserObjTest(){
        when(passwordEncoder.encode("ravi123")).thenReturn("HASHED");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.getUserObj("ravi@easypay.com", "ravi123", Role.EMPLOYEE);

        assertEquals("HASHED", saved.getPassword());
        assertEquals(Role.EMPLOYEE, saved.getRole());

        verify(passwordEncoder, times(1)).encode("ravi123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void deactivateTest(){
        User user = getUser("ravi@easypay.com", Role.EMPLOYEE);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        userService.deactivate(2L);

        assertFalse(user.isActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void deactivateTest_LastAdmin(){
        User admin = getUser("admin@easypay.com", Role.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.countActiveUsersByRole(Role.ADMIN)).thenReturn(1L);

        assertThrows(InvalidCallException.class, ()-> userService.deactivate(1L));
        assertTrue(admin.isActive());
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    public void changeRoleTest_EmployeeCannotBeAdmin(){
        User user = getUser("ravi@easypay.com", Role.EMPLOYEE);
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        // this user has an employee record, so ADMIN is not allowed
        when(employeeRepository.getByUserId(3L)).thenReturn(new Employee());

        assertThrows(InvalidCallException.class, ()-> userService.changeRole(3L, Role.ADMIN));
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    public void getUserDetailsTest(){
        // mock
        when(userRepository.getUserDetails("admin@easypay.com"))
                .thenReturn(new DetailsDto("admin@easypay.com", true, Role.ADMIN));

        // actual call
        DetailsDto detailsDto = userService.getUserDetails("admin@easypay.com");

        assertEquals("admin@easypay.com", detailsDto.username());
        assertEquals(Role.ADMIN, detailsDto.role());
        verify(userRepository, times(1)).getUserDetails("admin@easypay.com");
        verifyNoMoreInteractions(userRepository);
    }
}
