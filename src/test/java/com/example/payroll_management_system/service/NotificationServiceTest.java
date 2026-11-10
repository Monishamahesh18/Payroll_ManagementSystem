package com.example.payroll_management_system.service;

import com.example.payroll_management_system.enums.NotificationType;
import com.example.payroll_management_system.enums.Role;
import com.example.payroll_management_system.exceptions.InvalidCallException;
import com.example.payroll_management_system.exceptions.OwnershipException;
import com.example.payroll_management_system.model.Notification;
import com.example.payroll_management_system.model.User;
import com.example.payroll_management_system.repository.NotificationRepository;
import com.example.payroll_management_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    private Notification getNotification(String email){
        Notification notification = new Notification();
        notification.setId(4L);
        notification.setTitle("Your leave is APPROVED");
        notification.setType(NotificationType.LEAVE);
        notification.setUser(new User(email, "hashed", Role.EMPLOYEE));
        return notification;
    }

    @Test
    public void markReadTest(){
        Notification notification = getNotification("ravi@easypay.com");
        when(notificationRepository.findById(4L)).thenReturn(Optional.of(notification));

        notificationService.markRead(4L, "ravi@easypay.com");

        assertTrue(notification.isRead());
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    public void markReadTest_OwnershipCheck(){
        // this notification is of ravi, anita is logged in
        Notification notification = getNotification("ravi@easypay.com");
        when(notificationRepository.findById(4L)).thenReturn(Optional.of(notification));

        assertThrows(OwnershipException.class, ()-> notificationService.markRead(4L, "anita@easypay.com"));
        assertFalse(notification.isRead());
        verify(notificationRepository, times(0)).save(any(Notification.class));
    }

    @Test
    public void markReadTest_AlreadyRead(){
        Notification notification = getNotification("ravi@easypay.com");
        notification.setRead(true);
        when(notificationRepository.findById(4L)).thenReturn(Optional.of(notification));

        assertThrows(InvalidCallException.class, ()-> notificationService.markRead(4L, "ravi@easypay.com"));

        verify(notificationRepository, times(1)).findById(4L);
        verify(notificationRepository, times(0)).save(any(Notification.class));
    }
}
