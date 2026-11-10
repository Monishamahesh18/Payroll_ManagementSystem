package com.example.payroll_management_system.service;

import com.example.payroll_management_system.dto.requestDto.PaycomponentDto;
import com.example.payroll_management_system.dto.requestDto.PaycomponentUpdateDto;
import com.example.payroll_management_system.dto.responseDto.PaycomponentRespDto;
import com.example.payroll_management_system.enums.ComponentType;
import com.example.payroll_management_system.exceptions.InvalidCallException;
import com.example.payroll_management_system.exceptions.ResourceNotFoundException;
import com.example.payroll_management_system.model.PayComponent;
import com.example.payroll_management_system.repository.PaycomponentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaycomponentServiceTest {

    @InjectMocks
    private PaycomponentService paycomponentService;

    @Mock
    private PaycomponentRepository paycomponentRepository;

    private PayComponent getPaycomponent(Long id, String name, ComponentType type, Double value){
        PayComponent paycomponent = new PayComponent();
        paycomponent.setId(id);
        paycomponent.setName(name);
        paycomponent.setType(type);
        paycomponent.setValue(value);
        return paycomponent;
    }

    @Test
    public void addTest(){
        // mock
        PaycomponentDto dto = new PaycomponentDto("PF", ComponentType.DEDUCTION, 2000.0, true);

        // actual call
        paycomponentService.add(dto);

        // the pay component that goes to DB has the values of the dto
        ArgumentCaptor<PayComponent> captor = ArgumentCaptor.forClass(PayComponent.class);
        verify(paycomponentRepository, times(1)).save(captor.capture());
        assertEquals("PF", captor.getValue().getName());
        assertEquals(ComponentType.DEDUCTION, captor.getValue().getType());
        assertEquals(2000.0, captor.getValue().getValue(), 0.001);
        assertTrue(captor.getValue().isStatutory());
    }

    @Test
    public void getAllTest(){
        when(paycomponentRepository.findAll()).thenReturn(List.of(
                getPaycomponent(1L, "Bonus", ComponentType.EARNING, 2000.0),
                getPaycomponent(2L, "PF", ComponentType.DEDUCTION, 2000.0)));

        List<PaycomponentRespDto> list = paycomponentService.getAll();

        assertEquals(2, list.size());
        assertEquals("Bonus", list.get(0).name());
        assertEquals("PF", list.get(1).name());
        verify(paycomponentRepository, times(1)).findAll();
    }

    @Test
    public void getByTypeTest(){
        when(paycomponentRepository.findByType(ComponentType.DEDUCTION)).thenReturn(List.of(
                getPaycomponent(2L, "PF", ComponentType.DEDUCTION, 2000.0)));

        List<PaycomponentRespDto> list = paycomponentService.getByType(ComponentType.DEDUCTION);

        assertEquals(1, list.size());
        assertEquals(ComponentType.DEDUCTION, list.get(0).type());
        verify(paycomponentRepository, times(1)).findByType(ComponentType.DEDUCTION);
    }

    @Test
    public void updateTest(){
        PayComponent paycomponent = getPaycomponent(5L, "Insurance", ComponentType.BENEFIT, 500.0);
        when(paycomponentRepository.findById(5L)).thenReturn(Optional.of(paycomponent));

        paycomponentService.update(5L, new PaycomponentUpdateDto(600.0));

        assertEquals(600.0, paycomponent.getValue(), 0.001);
        verify(paycomponentRepository, times(1)).save(paycomponent);
    }

    @Test
    public void updateTest_NotFound(){
        when(paycomponentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                ()-> paycomponentService.update(99L, new PaycomponentUpdateDto(600.0)));
        verify(paycomponentRepository, times(0)).save(any(PayComponent.class));
    }

    @Test
    public void updateTest_NothingToUpdate(){
        // the dto has no value, so there is nothing to change
        PayComponent paycomponent = getPaycomponent(5L, "Insurance", ComponentType.BENEFIT, 500.0);
        when(paycomponentRepository.findById(5L)).thenReturn(Optional.of(paycomponent));

        assertThrows(InvalidCallException.class,
                ()-> paycomponentService.update(5L, new PaycomponentUpdateDto(null)));
        verify(paycomponentRepository, times(0)).save(any(PayComponent.class));
    }

    @Test
    public void setActiveTest(){
        // retire an active pay component
        PayComponent paycomponent = getPaycomponent(5L, "Insurance", ComponentType.BENEFIT, 500.0);
        when(paycomponentRepository.findById(5L)).thenReturn(Optional.of(paycomponent));

        paycomponentService.setActive(5L, false);

        assertFalse(paycomponent.isActive());
        verify(paycomponentRepository, times(1)).save(paycomponent);
    }

    @Test
    public void setActiveTest_AlreadyInThatState(){
        // a new pay component is active, so it cannot be made active again
        PayComponent paycomponent = getPaycomponent(5L, "Insurance", ComponentType.BENEFIT, 500.0);
        when(paycomponentRepository.findById(5L)).thenReturn(Optional.of(paycomponent));

        assertThrows(InvalidCallException.class, ()-> paycomponentService.setActive(5L, true));
        verify(paycomponentRepository, times(0)).save(any(PayComponent.class));
    }

    @Test
    public void setActiveTest_NotFound(){
        when(paycomponentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, ()-> paycomponentService.setActive(99L, false));

        verify(paycomponentRepository, times(1)).findById(99L);
        verify(paycomponentRepository, times(0)).save(any(PayComponent.class));
    }
}
