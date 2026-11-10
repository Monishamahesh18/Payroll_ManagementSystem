package com.example.payroll_management_system.mapper;

import com.example.payroll_management_system.dto.responseDto.PayslipDetailRespDto;
import com.example.payroll_management_system.dto.responseDto.PayslipItemRespDto;
import com.example.payroll_management_system.dto.responseDto.PayslipRespDto;
import com.example.payroll_management_system.model.Payslip;
import com.example.payroll_management_system.model.PayslipItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PayslipMapper {
    public static PayslipRespDto convertEntityToDto(Payslip payslip){
        return
                new PayslipRespDto(
                        payslip.getId(),
                        payslip.getRun().getId(),
                        payslip.getRun().getPeriodStart(),
                        payslip.getRun().getPeriodEnd(),
                        payslip.getEmployee().getId(),
                        payslip.getEmployee().getFullName(),
                        payslip.getGross(),
                        payslip.getDeduction(),
                        payslip.getNet(),
                        payslip.getPayStatus(),
                        payslip.getPaidOn()
                );
    }

    // pay stub: payslip totals plus every item line (earning, deduction, benefit)
    public static PayslipDetailRespDto convertEntityToDetailDto(Payslip payslip, List<PayslipItem> items){
        return
                new PayslipDetailRespDto(
                        payslip.getId(),
                        payslip.getRun().getId(),
                        payslip.getRun().getPeriodStart(),
                        payslip.getRun().getPeriodEnd(),
                        payslip.getEmployee().getId(),
                        payslip.getEmployee().getFullName(),
                        payslip.getEmployee().getBankAcc(),
                        payslip.getGross(),
                        payslip.getDeduction(),
                        payslip.getNet(),
                        payslip.getPayStatus(),
                        payslip.getPaidOn(),
                        items
                                .stream()
                                .map(PayslipMapper :: convertItemToDto)
                                .toList()
                );
    }

    public static PayslipItemRespDto convertItemToDto(PayslipItem item){
        return
                new PayslipItemRespDto(
                        item.getName(),
                        item.getType(),
                        item.getAmount(),
                        item.isStatutory()
                );
    }
}
