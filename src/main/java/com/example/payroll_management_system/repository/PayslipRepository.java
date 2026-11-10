package com.example.payroll_management_system.repository;

import com.example.payroll_management_system.dto.responseDto.PayslipRespDto;
import com.example.payroll_management_system.model.Payslip;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface PayslipRepository extends JpaRepository<Payslip,Long> {
    @Query("""
            select new com.example.payroll_management_system.dto.responseDto.PayslipRespDto(
            p.id,
            r.id,
            r.periodStart,
            r.periodEnd,
            e.id,
            e.fullName,
            p.gross,
            p.deduction,
            p.net,
            p.payStatus,
            p.paidOn
            )
            from Payslip p
            JOIN p.run r
            JOIN p.employee e
            where r.id = ?1
            order by e.fullName asc
            """)
    List<PayslipRespDto> getPayslipByRunId(Long runId, Pageable pageable);

    @Query("""
        select new com.example.payroll_management_system.dto.responseDto.PayslipRespDto(
        p.id,
        r.id,
        r.periodStart,
        r.periodEnd,
        e.id,
        e.fullName,
        p.gross,
        p.deduction,
        p.net,
        p.payStatus,
        p.paidOn
        )
        from Payslip p
        JOIN p.run r
        JOIN p.employee e
        where e.id = ?1
        order by r.periodStart desc
        """)
    List<PayslipRespDto> getPayslipByEmployeeId(Long employeeId, Pageable pageable);

    // payslips of one employee in one year. used by version 2 of payslip/my
    @Query("""
        select new com.example.payroll_management_system.dto.responseDto.PayslipRespDto(
        p.id,
        r.id,
        r.periodStart,
        r.periodEnd,
        e.id,
        e.fullName,
        p.gross,
        p.deduction,
        p.net,
        p.payStatus,
        p.paidOn
        )
        from Payslip p
        JOIN p.run r
        JOIN p.employee e
        where e.id = ?1 AND r.periodEnd between ?2 AND ?3
        order by r.periodStart desc
        """)
    List<PayslipRespDto> getPayslipByEmployeeIdAndYear(Long employeeId, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    List<Payslip> findByRunId(Long runId);

    // payslips of the manager's team. run id is optional - null gives every run
    @Query("""
        select new com.example.payroll_management_system.dto.responseDto.PayslipRespDto(
        p.id,
        r.id,
        r.periodStart,
        r.periodEnd,
        e.id,
        e.fullName,
        p.gross,
        p.deduction,
        p.net,
        p.payStatus,
        p.paidOn
        )
        from Payslip p
        JOIN p.run r
        JOIN p.employee e
        JOIN e.manager m
        where m.id = ?1 AND (?2 IS NULL OR r.id = ?2)
        order by r.periodStart desc, e.fullName asc
        """)
    List<PayslipRespDto> getPayslipByManagerId(Long managerId, Long runId, Pageable pageable);

    // total net pay of the manager's team in one run
    @Query("""
        select coalesce(sum(p.net), 0.0)
        from Payslip p
        JOIN p.run r
        JOIN p.employee e
        JOIN e.manager m
        where m.id = ?1 AND r.id = ?2
        """)
    Double sumTeamNetByRun(Long managerId, Long runId);

    // used by reprocess. delete the payslip items first, then the payslips
    @Modifying(flushAutomatically = true)
    @Query("delete from Payslip p where p.run.id = ?1")
    void deletePayslipsByRunId(Long runId);
}
