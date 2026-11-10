package com.example.payroll_management_system.repository;

import com.example.payroll_management_system.dto.responseDto.TimeSheetRespDto;
import com.example.payroll_management_system.enums.TimesheetStatus;
import com.example.payroll_management_system.model.Timesheet;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {

    // to check if this employee already gave timesheet for this date
    boolean existsByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    @Query("""
        select new com.example.payroll_management_system.dto.responseDto.TimeSheetRespDto(
        t.id,
        t.workDate,
        t.hours,
        t.status,
        e.id,
        e.fullName
        )
        from Timesheet t
        JOIN t.employee e
        where e.id = ?1
        order by t.workDate desc
        """)

    List<TimeSheetRespDto> getTimesheetByEmployeeId(Long employeeId, Pageable pageable);

    // all timesheets between two dates (used to lock a period)
    List<Timesheet> findByWorkDateBetween(LocalDate from, LocalDate to);

    long countByStatus(TimesheetStatus status);

    @Query("""
        select new com.example.payroll_management_system.dto.responseDto.TimeSheetRespDto(
        t.id,
        t.workDate,
        t.hours,
        t.status,
        e.id,
        e.fullName
        )
        from Timesheet t
        JOIN t.employee e
        where t.workDate >= ?1 AND t.workDate <= ?2
        order by t.workDate desc, e.fullName asc
        """)
    List<TimeSheetRespDto> getTimesheetByPeriod(LocalDate fromDate, LocalDate toDate, Pageable pageable);
}
