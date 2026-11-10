package com.example.payroll_management_system.service;

import com.example.payroll_management_system.dto.responseDto.AuditLogRespDto;
import com.example.payroll_management_system.exceptions.InvalidCallException;
import com.example.payroll_management_system.model.AuditLog;
import com.example.payroll_management_system.model.User;
import com.example.payroll_management_system.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    // other services call this method when something important is done (payroll approved, leave reviewed etc.)
    // user is the person who did the action
    public void log(User user, String action, String entity, Long entityId) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntity(entity);
        auditLog.setEntityId(entityId);
        auditLog.setUser(user);
        auditLogRepository.save(auditLog);
        log.info("AUDIT {} on {} {}", action, entity, entityId);
    }

    public List<AuditLogRespDto> getAll(Long userId, String action, LocalDate fromDate, LocalDate toDate,
                                        Integer page, Integer size) {
        // Step 1: from date cannot be after to date
        if(fromDate != null && toDate != null && fromDate.isAfter(toDate))
            throw new InvalidCallException("From date cannot be after to date");

        // Step 2: create Pageable using page and size values thru PageRequest
        Pageable pageable = PageRequest.of(page, size);

        // Step 3: blank action is treated as not given
        String actionFilter = (action == null || action.trim().isEmpty()) ? null : action.trim();

        // Step 4: get the logs (query gives the dto directly)
        return auditLogRepository.dynamicFilterAuditLog(userId, actionFilter,
                fromDate == null ? null : fromDate.atStartOfDay(ZoneOffset.UTC).toInstant(),
                toDate == null ? null : toDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant(),
                pageable);
    }
}
