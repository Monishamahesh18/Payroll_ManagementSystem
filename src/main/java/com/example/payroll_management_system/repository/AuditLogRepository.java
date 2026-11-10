package com.example.payroll_management_system.repository;

import com.example.payroll_management_system.dto.responseDto.AuditLogRespDto;
import com.example.payroll_management_system.model.AuditLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // dynamic filter - every parameter is optional
    @Query("""
        select new com.example.payroll_management_system.dto.responseDto.AuditLogRespDto(
        a.id,
        a.action,
        a.entity,
        a.entityId,
        a.createdAt,
        u.id,
        u.email
        )
        from AuditLog a
        JOIN a.user u
        where (?1 IS NULL OR u.id = ?1)
        AND (?2 IS NULL OR a.action = ?2)
        AND (?3 IS NULL OR a.createdAt >= ?3)
        AND (?4 IS NULL OR a.createdAt < ?4)
        order by a.createdAt desc
        """)
    List<AuditLogRespDto> dynamicFilterAuditLog(Long userId, String action, Instant fromDate, Instant toDate, Pageable pageable);
}
