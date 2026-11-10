package com.example.payroll_management_system.mapper;

import com.example.payroll_management_system.dto.requestDto.PaycomponentDto;
import com.example.payroll_management_system.dto.responseDto.PaycomponentRespDto;
import com.example.payroll_management_system.model.PayComponent;
import org.springframework.stereotype.Component;

@Component
public class PaycomponentMapper {

    public static PayComponent convertDtoToEntity(PaycomponentDto dto){
        PayComponent paycomponent = new PayComponent();
        paycomponent.setName(dto.name());
        paycomponent.setType(dto.type());
        paycomponent.setValue(dto.value());
        paycomponent.setStatutory(dto.isStatutory());
        return paycomponent;
    }
    public static PaycomponentRespDto convertEntityToDto(PayComponent paycomponent){
        return
                new PaycomponentRespDto(
                        paycomponent.getId(),
                        paycomponent.getName(),
                        paycomponent.getType(),
                        paycomponent.getValue(),
                        paycomponent.isStatutory(),
                        paycomponent.isActive()
                );
    }
}
