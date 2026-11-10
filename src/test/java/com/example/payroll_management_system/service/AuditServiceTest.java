package com.example.payroll_management_system.service;

import com.example.payroll_management_system.dto.responseDto.AuditLogRespDto;
import com.example.payroll_management_system.enums.Role;
import com.example.payroll_management_system.exceptions.InvalidCallException;
import com.example.payroll_management_system.model.AuditLog;
import com.example.payroll_management_system.model.User;
import com.example.payroll_management_system.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditServiceTest {

    @InjectMocks
    private AuditService auditService;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Test
    public void logTest(){
        // mock
        User processor = new User("manasag@easypay.com", "hashed", Role.PROCESSOR);

        // actual call
        auditService.log(processor, "PAYROLL_PAID", "PAYROLL_RUN", 3L);

        // the audit entry that goes to DB has all the four values
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());
        assertEquals("PAYROLL_PAID", captor.getValue().getAction());
        assertEquals("PAYROLL_RUN", captor.getValue().getEntity());
        assertEquals(Long.valueOf(3L), captor.getValue().getEntityId());
        assertEquals(processor, captor.getValue().getUser());
    }

    @Test
    public void getAllTest(){
        // mock: the from date becomes the start of that day, the to date moves one day ahead
        Instant from = LocalDate.of(2026, 11, 1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = LocalDate.of(2026, 12, 1).atStartOfDay(ZoneOffset.UTC).toInstant();
        AuditLogRespDto row = new AuditLogRespDto(1L, "PAYROLL_PAID", "PAYROLL_RUN", 3L,
                Instant.now(), 2L, "manasag@easypay.com");
        when(auditLogRepository.dynamicFilterAuditLog(2L, "PAYROLL_PAID", from, to, PageRequest.of(0, 10)))
                .thenReturn(List.of(row));

        // actual call. the action has spaces around it
        List<AuditLogRespDto> list = auditService.getAll(2L, " PAYROLL_PAID ",
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), 0, 10);

        assertEquals(1, list.size());
        assertEquals("PAYROLL_PAID", list.get(0).action());
        verify(auditLogRepository, times(1))
                .dynamicFilterAuditLog(2L, "PAYROLL_PAID", from, to, PageRequest.of(0, 10));
    }

    @Test
    public void getAllTest_NoFilters(){
        // a blank action and no dates mean no filter, so the repository gets nulls
        when(auditLogRepository.dynamicFilterAuditLog(null, null, null, null, PageRequest.of(0, 10)))
                .thenReturn(List.of());

        List<AuditLogRespDto> list = auditService.getAll(null, "   ", null, null, 0, 10);

        assertEquals(0, list.size());
        verify(auditLogRepository, times(1))
                .dynamicFilterAuditLog(null, null, null, null, PageRequest.of(0, 10));
    }

    @Test
    public void getAllTest_DatesInWrongOrder(){
        // from date is after the to date
        assertThrows(InvalidCallException.class,
                ()-> auditService.getAll(null, null, LocalDate.of(2026, 11, 30), LocalDate.of(2026, 11, 1), 0, 10));

        verifyNoMoreInteractions(auditLogRepository);
    }
}
