package com.example.payroll_management_system.mapper;

import com.example.payroll_management_system.dto.requestDto.AdminDto;
import com.example.payroll_management_system.model.User;
import org.springframework.stereotype.Component;

@Component
public class AdminMapper {


    public static User convertDtoToEntity(AdminDto dto){
        User user = new User();
        user.setEmail(dto.email());
        user.setPassword(dto.password());
        return user;
    }
}
