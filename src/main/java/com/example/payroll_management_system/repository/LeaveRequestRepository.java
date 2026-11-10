package com.example.payroll_management_system.repository;

import com.example.payroll_management_system.dto.responseDto.LeaveRespDto;
import com.example.payroll_management_system.enums.LeaveStatus;
import com.example.payroll_management_system.model.LeaveRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    @Query("""
        select new com.example.payroll_management_system.dto.responseDto.LeaveRespDto(
        l.id,
        l.fromDate,
        l.toDate,
        l.type,
        l.status,
        e.id,
        e.fullName,
        r.fullName
        )
        from LeaveRequest l
        JOIN l.employee e
        LEFT JOIN l.reviewedBy r
        where e.id = ?1
        order by l.fromDate desc
        """)
    List<LeaveRespDto> getLeaveByEmployeeId(Long employeeId, Pageable pageable);

    @Query("""
        select new com.example.payroll_management_system.dto.responseDto.LeaveRespDto(
        l.id,
        l.fromDate,
        l.toDate,
        l.type,
        l.status,
        e.id,
        e.fullName,
        r.fullName
        )
        from LeaveRequest l
        JOIN l.employee e
        JOIN e.manager m
        LEFT JOIN l.reviewedBy r
        where m.id = ?1 AND l.status = ?2
        order by l.fromDate asc
        """)
    List<LeaveRespDto> getLeaveByManagerIdAndStatus(Long managerId, LeaveStatus leaveStatus, Pageable pageable);

    // counts the leaves of this employee (not rejected) that touch the given dates
    @Query("""
        select count(l) from LeaveRequest l
        JOIN l.employee e
        where e.id = ?1 AND l.status <> ?2
        AND l.fromDate <= ?4 AND l.toDate >= ?3
        """)
    long countOverlappingLeaves(Long employeeId, LeaveStatus status, LocalDate fromDate, LocalDate toDate);

    // all leaves of the manager's team. status is optional - null gives every status
    @Query("""
        select new com.example.payroll_management_system.dto.responseDto.LeaveRespDto(
        l.id,
        l.fromDate,
        l.toDate,
        l.type,
        l.status,
        e.id,
        e.fullName,
        r.fullName
        )
        from LeaveRequest l
        JOIN l.employee e
        JOIN e.manager m
        LEFT JOIN l.reviewedBy r
        where m.id = ?1 AND (?2 IS NULL OR l.status = ?2)
        order by l.fromDate desc
        """)
    List<LeaveRespDto> getLeaveByManagerId(Long managerId, LeaveStatus status, Pageable pageable);

    // leaves with the given status that touch the given period (used for leave balance)
    @Query("""
        select l from LeaveRequest l
        JOIN l.employee e
        where e.id = ?1 AND l.status = ?2
        AND l.fromDate <= ?4 AND l.toDate >= ?3
        """)
    List<LeaveRequest> getLeavesInPeriod(Long employeeId, LeaveStatus status, LocalDate periodStart, LocalDate periodEnd);

    long countByStatus(LeaveStatus status);

    @Query("""
        select count(l) from LeaveRequest l
        JOIN l.employee e
        JOIN e.manager m
        where m.id = ?1 AND l.status = ?2
        """)
    long countTeamLeavesByStatus(Long managerId, LeaveStatus status);
}
