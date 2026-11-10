package com.example.payroll_management_system.repository;

import com.example.payroll_management_system.enums.RunStatus;
import com.example.payroll_management_system.model.PayrollRun;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PayrollrunRepository extends JpaRepository<PayrollRun, Long> {
    // counts the runs whose period touches the given period
    @Query("""
            select count(r) from PayrollRun r
            where r.periodStart <= ?2 AND r.periodEnd >= ?1
            """)
    long countOverlappingRuns(LocalDate periodStart, LocalDate periodEnd);

    List<PayrollRun> findByStatus(RunStatus status, Pageable pageable);

    long countByStatus(RunStatus status);

    // the latest run (highest id). null if there is no run yet
    PayrollRun findTopByOrderByIdDesc();
}
