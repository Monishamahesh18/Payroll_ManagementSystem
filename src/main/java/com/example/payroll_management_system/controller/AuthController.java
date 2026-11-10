package com.example.payroll_management_system.controller;

import com.example.payroll_management_system.dto.responseDto.DetailsDto;
import com.example.payroll_management_system.dto.responseDto.TokenDto;
import com.example.payroll_management_system.service.UserService;
import com.example.payroll_management_system.utility.JwtUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")
@Slf4j
public class AuthController {

    private final JwtUtility jwtUtility;
    private final UserService userService;

    /* This api is authenticated by Spring. which means if the control reaches this method, Spring
     * already has checked the credentials and it can share the username */
    @GetMapping("/api/auth/login")
    public TokenDto login(Principal principal){
        String loggedInUsername = principal.getName();
        String token = jwtUtility.generateToken(loggedInUsername);
        log.info("Login success: {}", loggedInUsername);
        return new TokenDto(token,
                jwtUtility.extractExpiration(token),
                LocalDateTime.now(ZoneId.of("UTC")).toString());
    }

    /*
     * Fetch User details based on the given token
     * If this method is called, the user has given the token to Spring and
     * I can ask spring for logged-in username.
     * */
    @GetMapping("/api/auth/user-details")
    public DetailsDto getUserDetails(Principal principal){
        String loggedInUsername = principal.getName();
        return userService.getUserDetails(loggedInUsername);
    }
}
