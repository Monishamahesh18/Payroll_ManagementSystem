package com.example.payroll_management_system.repository;

import com.example.payroll_management_system.dto.responseDto.DepartmentStatRespDto;
import com.example.payroll_management_system.dto.responseDto.EmployeeRespDto;
import com.example.payroll_management_system.enums.EmployeeStatus;
import com.example.payroll_management_system.model.Employee;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("""
        select new com.example.payroll_management_system.dto.responseDto.EmployeeRespDto(
        e.id,
        e.empCode,
        e.fullName,
        e.department,
        e.joinDate,
        e.status,
        e.basic,
        e.hra,
        e.allowance,
        e.bankAcc,
        e.phone,
        e.address,
        e.taxWithholding,
        u.email,
        u.role,
        m.id,
        m.fullName
        )
        from Employee e
        JOIN e.user u
        JOIN e.manager m
        where m.id = ?1
        """)
    List<EmployeeRespDto> getTeamByManagerId(Long managerId, Pageable pageable);

    // finds the employee linked with this login (user). null if the user is admin or processor
    @Query("""
        select e from Employee e
        JOIN e.user u
        where u.id = ?1
        """)
    Employee getByUserId(Long userId);

    // only ACTIVE employees who joined on or before the given date get a payslip
    List<Employee> findByStatusAndJoinDateLessThanEqual(EmployeeStatus status, LocalDate date);

    long countByStatus(EmployeeStatus status);

    @Query("""
        select count(e) from Employee e
        JOIN e.manager m
        where m.id = ?1
        """)
    long countTeam(Long managerId);

    @Query("""
        select count(e) from Employee e
        JOIN e.manager m
        where m.id = ?1 AND e.status = ?2
        """)
    long countTeamByStatus(Long managerId, EmployeeStatus status);

    // dynamic filter - department and status are optional
    @Query("""
        select new com.example.payroll_management_system.dto.responseDto.EmployeeRespDto(
        e.id,
        e.empCode,
        e.fullName,
        e.department,
        e.joinDate,
        e.status,
        e.basic,
        e.hra,
        e.allowance,
        e.bankAcc,
        e.phone,
        e.address,
        e.taxWithholding,
        u.email,
        u.role,
        m.id,
        m.fullName
        )
        from Employee e
        JOIN e.user u
        LEFT JOIN e.manager m
        where (?1 IS NULL OR e.department = ?1)
        AND (?2 IS NULL OR e.status = ?2)
        order by e.id
        """)
    List<EmployeeRespDto> dynamicFilterEmployee(String department, EmployeeStatus status, Pageable pageable);

    // group by - number of employees in each department
    @Query("""
        select e.department as department, count(e.id) as numberOfEmployees
        from Employee e
        group by e.department
        order by e.department
        """)
    List<DepartmentStatRespDto> getDepartmentStat();

    @Query("""
        select e from Employee e
        JOIN e.user u
        where u.email = ?1
        """)
    Employee getByUserEmail(String email);
}
