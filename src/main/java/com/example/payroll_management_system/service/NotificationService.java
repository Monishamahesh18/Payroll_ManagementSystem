package com.example.payroll_management_system.service;

import com.example.payroll_management_system.dto.responseDto.NotificationRespDto;
import com.example.payroll_management_system.enums.NotificationType;
import com.example.payroll_management_system.exceptions.InvalidCallException;
import com.example.payroll_management_system.exceptions.OwnershipException;
import com.example.payroll_management_system.exceptions.ResourceNotFoundException;
import com.example.payroll_management_system.model.Notification;
import com.example.payroll_management_system.model.User;
import com.example.payroll_management_system.repository.NotificationRepository;
import com.example.payroll_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // other services call this method when something important happens (leave reviewed, salary paid etc.)
    public void create(User user, String title, NotificationType type) {
        // Step 1: title column is 150 characters, so cut the extra text
        String shortTitle = title.length() > 150 ? title.substring(0, 150) : title;

        // Step 2: prepare Notification object, new notification is always unread
        Notification notification = new Notification();
        notification.setTitle(shortTitle);
        notification.setType(type);
        notification.setRead(false);
        notification.setUser(user);

        // Step 3: save in DB
        notificationRepository.save(notification);
        log.info("Message created: {}",title);
    }

    // notifications of the logged in user (the email comes from the token)
    public List<NotificationRespDto> getByEmail(String email, Boolean isRead, Integer page, Integer size) {
        // Step 1: get the logged in user from DB
        User user = userRepository.loadUserByUsername(email, true);
        if(user == null)
            throw new ResourceNotFoundException("User Not Found");

        // Step 2: create Pageable using page and size values thru PageRequest
        Pageable pageable = PageRequest.of(page, size);

        // Step 3: get the notifications of this user, latest first (query gives the dto directly)
        return notificationRepository.getNotificationByUserId(user.getId(), isRead, pageable);
    }

    public void markRead(Long id, String email) {
        // Step 1: get the notification from DB using the id given in url
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Notification Not Found"));

        // Step 1.5: the notification should belong to the logged in user
        if(!notification.getUser().getEmail().equals(email))
            throw new OwnershipException("Operation not allowed! contact admin");

        // Step 2: nothing to do if it is already read
        if(notification.isRead())
            throw new InvalidCallException("Notification is already read");

        // Step 3: mark as read and save
        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
