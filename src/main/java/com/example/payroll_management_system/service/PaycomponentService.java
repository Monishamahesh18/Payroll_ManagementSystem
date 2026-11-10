package com.example.payroll_management_system.service;

import com.example.payroll_management_system.dto.requestDto.PaycomponentDto;
import com.example.payroll_management_system.dto.requestDto.PaycomponentUpdateDto;
import com.example.payroll_management_system.dto.responseDto.PaycomponentRespDto;
import com.example.payroll_management_system.enums.ComponentType;
import com.example.payroll_management_system.exceptions.InvalidCallException;
import com.example.payroll_management_system.exceptions.ResourceNotFoundException;
import com.example.payroll_management_system.mapper.PaycomponentMapper;
import com.example.payroll_management_system.model.PayComponent;
import com.example.payroll_management_system.repository.PaycomponentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaycomponentService {

    private final PaycomponentRepository paycomponentRepository;

    public void add(PaycomponentDto paycomponentDto) {
        // Step 1: change PaycomponentDto to Paycomponent entity using mapper
        PayComponent paycomponent = PaycomponentMapper.convertDtoToEntity(paycomponentDto);

        // Step 2: save pay component in DB
        paycomponentRepository.save(paycomponent);
        log.info("Pay component added: {}", paycomponentDto.name());
    }

    public List<PaycomponentRespDto> getAll() {
        // Step 1: get all pay components from DB
        List<PayComponent> list = paycomponentRepository.findAll();

        // Step 2: Map List<Paycomponent> to List<PaycomponentRespDto> using Mapper
        return list
                .stream()
                .map(PaycomponentMapper :: convertEntityToDto)
                .toList();
    }

    public List<PaycomponentRespDto> getByType(ComponentType type) {
        // Step 1: get the pay components of this type from DB
        List<PayComponent> list = paycomponentRepository.findByType(type);

        // Step 2: Map List<PayComponent> to List<PaycomponentRespDto> using Mapper
        return list
                .stream()
                .map(PaycomponentMapper :: convertEntityToDto)
                .toList();
    }

    public void update(Long id, PaycomponentUpdateDto dto) {
        // Step 1: get the pay component from DB using the id given in url
        PayComponent paycomponent = paycomponentRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Pay Component Not Found"));

        // Step 2: update only the fields that are sent
        boolean flag = false;

        if(!(dto.value() == null)) {
            paycomponent.setValue(dto.value());
            flag = true;
        }

        // Step 3: nothing was sent to update
        if(!flag)
            throw new InvalidCallException("Update op terminated");

        // Step 4: save in DB
        paycomponentRepository.save(paycomponent);
        log.info("Value of pay component {} is changed", id);
    }

    // retire (active = false) or re-activate a pay component. payroll uses only the active ones
    public void setActive(Long id, boolean active) {
        // Step 1: get the pay component from DB using the id given in url
        PayComponent paycomponent = paycomponentRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Pay Component Not Found"));

        // Step 2: it is already in the given state
        if(paycomponent.isActive() == active)
            throw new InvalidCallException("Pay component is already " + (active ? "active" : "inactive"));

        // Step 3: update and save in DB
        paycomponent.setActive(active);
        paycomponentRepository.save(paycomponent);
        log.info("Pay component {} active is now {}", id, active);
    }
}
