package com.example.payroll_management_system.service;

import com.example.payroll_management_system.enums.Role;
import com.example.payroll_management_system.model.User;
import com.example.payroll_management_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MyUserDetailsServiceTest {

    @InjectMocks
    private MyUserDetailsService myUserDetailsService;

    @Mock
    private UserRepository userRepository;

    @Test
    public void loadUserByUsernameTest(){
        // mock
        User user = new User("ravi@easypay.com", "hashed", Role.EMPLOYEE);
        when(userRepository.loadUserByUsername("ravi@easypay.com", true)).thenReturn(user);

        // actual call
        UserDetails userDetails = myUserDetailsService.loadUserByUsername("ravi@easypay.com");

        assertEquals("ravi@easypay.com", userDetails.getUsername());
        assertEquals("EMPLOYEE", userDetails.getAuthorities().iterator().next().getAuthority());
        verify(userRepository, times(1)).loadUserByUsername("ravi@easypay.com", true);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void loadUserByUsernameTest_NotActive(){
        // the query gives null when the email is wrong or the user is not active
        when(userRepository.loadUserByUsername("nobody@easypay.com", true)).thenReturn(null);

        assertNull(myUserDetailsService.loadUserByUsername("nobody@easypay.com"));
        verify(userRepository, times(1)).loadUserByUsername("nobody@easypay.com", true);
        verifyNoMoreInteractions(userRepository);
    }
}
