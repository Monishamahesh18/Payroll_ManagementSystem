package com.example.payroll_management_system.mapper;

import com.example.payroll_management_system.dto.requestDto.EmployeeDto;
import com.example.payroll_management_system.dto.responseDto.EmployeeRespDto;
import com.example.payroll_management_system.model.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {


    public static Employee convertDtoToEntity(EmployeeDto dto) {
        Employee employee = new Employee();
        employee.setEmpCode(dto.empCode());
        employee.setFullName(dto.fullName());
        employee.setDepartment(dto.department());
        employee.setJoinDate(dto.joinDate());
        employee.setBasic(dto.basic());
        employee.setHra(dto.hra());
        employee.setAllowance(dto.allowance());
        employee.setBankAcc(dto.bankAcc());
        employee.setPhone(dto.phone());
        employee.setAddress(dto.address());
        return employee;
    }

    public static EmployeeRespDto convertEntityToDto(Employee employee) {
        // manager is null for the top manager, so we check null before reading it
        return
                new EmployeeRespDto(
                        employee.getId(),
                        employee.getEmpCode(),
                        employee.getFullName(),
                        employee.getDepartment(),
                        employee.getJoinDate(),
                        employee.getStatus(),
                        employee.getBasic(),
                        employee.getHra(),
                        employee.getAllowance(),
                        employee.getBankAcc(),
                        employee.getPhone(),
                        employee.getAddress(),
                        employee.getTaxWithholding(),
                        employee.getUser().getEmail(),
                        employee.getUser().getRole(),
                        employee.getManager() == null ? null : employee.getManager().getId(),
                        employee.getManager() == null ? null : employee.getManager().getFullName()

                );
    }
}
