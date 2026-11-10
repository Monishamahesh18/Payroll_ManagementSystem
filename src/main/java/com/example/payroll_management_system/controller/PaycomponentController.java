package com.example.payroll_management_system.controller;

import com.example.payroll_management_system.dto.requestDto.PaycomponentDto;
import com.example.payroll_management_system.dto.requestDto.PaycomponentUpdateDto;
import com.example.payroll_management_system.dto.responseDto.PaycomponentRespDto;
import com.example.payroll_management_system.enums.ComponentType;
import com.example.payroll_management_system.service.PaycomponentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")
public class PaycomponentController {

    private final PaycomponentService paycomponentService;

    // Done by Admin
    @PostMapping("/api/paycomponent/add")
    public void add(@Valid @RequestBody PaycomponentDto paycomponentDto){

        paycomponentService.add(paycomponentDto);
    }

    // Done by Admin / Processor
    @GetMapping("/api/paycomponent/all")
    public List<PaycomponentRespDto> getAll(){
        return paycomponentService.getAll();
    }

    // Done by Admin / Processor
    @GetMapping("/api/paycomponent/type/{type}")
    public List<PaycomponentRespDto> getByType(@PathVariable ComponentType type){
        return paycomponentService.getByType(type);
    }

    // Done by Admin
    @PutMapping("/api/paycomponent/{id}")
    public void update(@PathVariable Long id,
                       @Valid @RequestBody PaycomponentUpdateDto paycomponentUpdateDto){
        paycomponentService.update(id, paycomponentUpdateDto);
    }

    // active=false retires the component, active=true brings it back
    // Done by Admin
    @PutMapping("/api/paycomponent/active/{id}")
    public void setActive(@PathVariable Long id,
                          @RequestParam("active") boolean active){
        paycomponentService.setActive(id, active);
    }
}
