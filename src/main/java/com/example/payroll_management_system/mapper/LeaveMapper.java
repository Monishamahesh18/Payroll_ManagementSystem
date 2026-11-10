package com.example.payroll_management_system.mapper;

import com.example.payroll_management_system.dto.requestDto.LeaveDto;
import com.example.payroll_management_system.model.LeaveRequest;
import org.springframework.stereotype.Component;

@Component
public class LeaveMapper {

    public static LeaveRequest convertDtoToEntity(LeaveDto dto){
        LeaveRequest leave = new LeaveRequest();
        leave.setFromDate(dto.fromDate());
        leave.setToDate(dto.toDate());
        leave.setType(dto.type());
        return leave;
    }
}
