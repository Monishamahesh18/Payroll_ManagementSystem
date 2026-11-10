package com.example.payroll_management_system.mapper;

import com.example.payroll_management_system.dto.requestDto.PayrollrunDto;
import com.example.payroll_management_system.dto.responseDto.PayrollrunRespDto;
import com.example.payroll_management_system.model.PayrollRun;
import org.springframework.stereotype.Component;

@Component
public class PayrollrunMapper {

    public static PayrollRun convertDtoToEntity(PayrollrunDto dto){
        PayrollRun run = new PayrollRun();
        run.setPeriodStart(dto.periodStart());
        run.setPeriodEnd(dto.periodEnd());
        return run;
    }
    public static PayrollrunRespDto convertEntityToDto(PayrollRun run){
        // processedBy and approvedBy are null for a new run, so we check null before reading email
        return
                new PayrollrunRespDto(
                        run.getId(),
                        run.getPeriodStart(),
                        run.getPeriodEnd(),
                        run.getStatus(),
                        run.getProcessedBy() == null ? null : run.getProcessedBy().getEmail(),
                        run.getApprovedBy() == null ? null : run.getApprovedBy().getEmail(),
                        run.getRemarks()
                );
    }
}
