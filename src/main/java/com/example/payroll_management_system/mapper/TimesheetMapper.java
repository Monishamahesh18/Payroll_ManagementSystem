package com.example.payroll_management_system.mapper;

import com.example.payroll_management_system.dto.requestDto.TimesheetDto;
import com.example.payroll_management_system.model.Timesheet;
import org.springframework.stereotype.Component;

@Component
public class TimesheetMapper {

    public static Timesheet convertDtoToEntity(TimesheetDto dto){
        Timesheet timesheet = new Timesheet();
        timesheet.setWorkDate(dto.workDate());
        timesheet.setHours(dto.hours());
        return timesheet;
    }
}
