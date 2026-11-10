package com.example.payroll_management_system.config;

import com.example.payroll_management_system.service.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final MyUserDetailsService myUserDetailsService ;
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        // the browser asks this before every call from the React app (CORS preflight)
                        .requestMatchers(HttpMethod.POST,"/api/admin/add").denyAll()
                        // login API
                        .requestMatchers(HttpMethod.GET,"/api/auth/login").authenticated()
                        // user details API
                        .requestMatchers(HttpMethod.GET,"/api/auth/user-details").authenticated()

                        // Admin onboard API (permitAll to add) (After addition - denyAll)
                        .requestMatchers(HttpMethod.POST,"/api/admin/add").permitAll()

                        // Processor
                        .requestMatchers(HttpMethod.POST,"/api/payrollrun/add").hasAuthority("PROCESSOR")
                        .requestMatchers(HttpMethod.PUT,"/api/payrollrun/calculate/{runId}").hasAuthority("PROCESSOR")
                        .requestMatchers(HttpMethod.PUT,"/api/payrollrun/submit/{runId}").hasAuthority("PROCESSOR")
                        .requestMatchers(HttpMethod.PUT,"/api/payrollrun/pay/{runId}").hasAuthority("PROCESSOR")
                        .requestMatchers(HttpMethod.GET,"/api/payrollrun/verify/{runId}").hasAuthority("PROCESSOR")
                        .requestMatchers(HttpMethod.GET,"/api/timesheet/period").hasAuthority("PROCESSOR")
                        .requestMatchers(HttpMethod.PUT,"/api/timesheet/lock").hasAuthority("PROCESSOR")
                        .requestMatchers(HttpMethod.GET,"/api/dashboard/processor").hasAuthority("PROCESSOR")

                        // Manager (own team)
                        .requestMatchers(HttpMethod.GET,"/api/employee/team").hasAuthority("MANAGER")
                        .requestMatchers(HttpMethod.GET,"/api/leave/pending").hasAuthority("MANAGER")
                        .requestMatchers(HttpMethod.GET,"/api/leave/team").hasAuthority("MANAGER")
                        .requestMatchers(HttpMethod.PUT,"/api/leave/review/{leaveId}").hasAuthority("MANAGER")
                        .requestMatchers(HttpMethod.GET,"/api/payslip/team").hasAuthority("MANAGER")
                        .requestMatchers(HttpMethod.GET,"/api/dashboard/manager").hasAuthority("MANAGER")

                        // Employee and Manager (own data, a manager is also an employee)
                        .requestMatchers(HttpMethod.POST,"/api/timesheet/add").hasAnyAuthority("EMPLOYEE","MANAGER")
                        .requestMatchers(HttpMethod.GET,"/api/timesheet/my").hasAnyAuthority("EMPLOYEE","MANAGER")
                        .requestMatchers(HttpMethod.PUT,"/api/timesheet/{id}").hasAnyAuthority("EMPLOYEE","MANAGER")
                        .requestMatchers(HttpMethod.POST,"/api/leave/add").hasAnyAuthority("EMPLOYEE","MANAGER")
                        .requestMatchers(HttpMethod.GET,"/api/leave/my").hasAnyAuthority("EMPLOYEE","MANAGER")
                        .requestMatchers(HttpMethod.DELETE,"/api/leave/cancel/{leaveId}").hasAnyAuthority("EMPLOYEE","MANAGER")
                        .requestMatchers(HttpMethod.GET,"/api/leave/balance").hasAnyAuthority("EMPLOYEE","MANAGER")
                        .requestMatchers(HttpMethod.GET,"/api/payslip/v1/my").hasAnyAuthority("EMPLOYEE","MANAGER")
                        .requestMatchers(HttpMethod.GET,"/api/payslip/v2/my").hasAnyAuthority("EMPLOYEE","MANAGER")                        .requestMatchers(HttpMethod.PUT,"/api/employee/personal").hasAnyAuthority("EMPLOYEE","MANAGER")
                        .requestMatchers(HttpMethod.GET,"/api/employee/me").hasAnyAuthority("EMPLOYEE","MANAGER")
                        // Admin
                        .requestMatchers(HttpMethod.POST,"/api/processor/add").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST,"/api/manager/add").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST,"/api/employee/add/{managerId}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/user/all").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/api/user/deactivate/{userId}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/api/user/activate/{userId}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/api/user/role/{userId}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/api/user/password/{userId}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/api/status/update/{employeeId}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/api/employee/{id}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST,"/api/paycomponent/add").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/api/paycomponent/active/{id}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/api/paycomponent/{id}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/employee/stat/department").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/report/compliance/run/{runId}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/dashboard/admin").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/api/payrollrun/approve/{runId}").hasAuthority("ADMIN")

                        // Admin and Processor
                        .requestMatchers(HttpMethod.PUT,"/api/payrollrun/reprocess/{runId}").hasAnyAuthority("ADMIN","PROCESSOR")
                        .requestMatchers(HttpMethod.GET,"/api/employee/all").hasAnyAuthority("ADMIN","PROCESSOR")
                        .requestMatchers(HttpMethod.GET,"/api/employee/filter").hasAnyAuthority("ADMIN","PROCESSOR")
                        .requestMatchers(HttpMethod.GET,"/api/employee/{id}").hasAnyAuthority("ADMIN","PROCESSOR")
                        .requestMatchers(HttpMethod.GET,"/api/paycomponent/all").hasAnyAuthority("ADMIN","PROCESSOR")
                        .requestMatchers(HttpMethod.GET,"/api/paycomponent/type/{type}").hasAnyAuthority("ADMIN","PROCESSOR")
                        .requestMatchers(HttpMethod.GET,"/api/payrollrun/all").hasAnyAuthority("ADMIN","PROCESSOR")
                        .requestMatchers(HttpMethod.GET,"/api/payrollrun/status/{status}").hasAnyAuthority("ADMIN","PROCESSOR")
                        .requestMatchers(HttpMethod.GET,"/api/payrollrun/{id}").hasAnyAuthority("ADMIN","PROCESSOR")
                        .requestMatchers(HttpMethod.GET,"/api/payslip/run/{runId}").hasAnyAuthority("ADMIN","PROCESSOR")
                        .requestMatchers(HttpMethod.GET,"/api/payslip/employee/{employeeId}").hasAnyAuthority("ADMIN","PROCESSOR")
                        .requestMatchers(HttpMethod.GET,"/api/payslip/stat/run/{runId}").hasAnyAuthority("ADMIN","PROCESSOR")
                        .requestMatchers(HttpMethod.GET,"/api/audit/all").hasAnyAuthority("ADMIN","PROCESSOR")

                        // Every logged in user (the service checks that the data is his own)
                        .requestMatchers(HttpMethod.GET,"/api/payslip/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET,"/api/notification/my").authenticated()
                        .requestMatchers(HttpMethod.PUT,"/api/notification/read/{id}").authenticated()
                        .anyRequest().permitAll()
                );
        // Add jwt filter before Spring's Auth Filter
        http.addFilterBefore(jwtFilter , UsernamePasswordAuthenticationFilter.class);
        http.httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public AuthenticationProvider getProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(myUserDetailsService);
        authenticationProvider.setPasswordEncoder(getEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder getEncoder(){
        return new BCryptPasswordEncoder();
    }
}
