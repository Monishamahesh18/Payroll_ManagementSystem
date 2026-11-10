package com.example.payroll_management_system.repository;

import com.example.payroll_management_system.dto.responseDto.ComplianceRespDto;
import com.example.payroll_management_system.model.PayslipItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayslipItemRepository extends JpaRepository<PayslipItem, Long> {

    List<PayslipItem> findByPayslipIdOrderByIdAsc(Long payslipId);

    // used by reprocess. delete the payslip items first, then the payslips
    @Modifying(flushAutomatically = true)
    @Query("delete from PayslipItem i where i.payslip.id in (select p.id from Payslip p where p.run.id = ?1)")
    void deleteItemsByRunId(Long runId);

    // group by - one line for every statutory item of the run
    @Query("""
        select i.name as itemName, i.type as componentType, count(i.id) as employeeCount, sum(i.amount) as totalAmount
        from PayslipItem i
        JOIN i.payslip p
        JOIN p.run r
        where r.id = ?1 AND i.isStatutory = true
        group by i.name, i.type
        order by i.name
        """)
    List<ComplianceRespDto> getComplianceByRunId(Long runId);
}
