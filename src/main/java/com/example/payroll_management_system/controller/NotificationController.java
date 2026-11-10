package com.example.payroll_management_system.controller;

import com.example.payroll_management_system.dto.responseDto.NotificationRespDto;
import com.example.payroll_management_system.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")
public class NotificationController {

    private final NotificationService notificationService;

    // notifications of the logged in user, so no id in the url
    // isRead is optional - nothing given means read and unread notifications
    // Done by every role
    @GetMapping("/api/notification/my")
    public List<NotificationRespDto> getMine(Principal principal,
                                             @RequestParam(name = "isRead", required = false) Boolean isRead,
                                             @RequestParam(name = "page" , required = false, defaultValue = "0") Integer page,
                                             @RequestParam(name = "size", required = false, defaultValue = "20") Integer size){
        return notificationService.getByEmail(principal.getName(), isRead, page, size);
    }

    // Done by every role (only own notification)
    @PutMapping("/api/notification/read/{id}")
    public void markRead(@PathVariable Long id,
                         Principal principal){
        notificationService.markRead(id, principal.getName());
    }
}
