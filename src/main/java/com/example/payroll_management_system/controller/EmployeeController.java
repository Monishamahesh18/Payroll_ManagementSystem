package com.example.payroll_management_system.controller;

import com.example.payroll_management_system.dto.requestDto.EmployeeDto;
import com.example.payroll_management_system.dto.requestDto.EmployeePersonalUpdateDto;
import com.example.payroll_management_system.dto.requestDto.EmployeeUpdateDto;
import com.example.payroll_management_system.dto.responseDto.DepartmentStatRespDto;
import com.example.payroll_management_system.dto.responseDto.EmployeeRespDto;
import com.example.payroll_management_system.enums.EmployeeStatus;
import com.example.payroll_management_system.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173/")
public class EmployeeController {

    private final EmployeeService employeeService;

    /*
     DTO (empCode,fullName,email...)
     validation
     Map DTO to Entity
     Insert Entity IN Db
     * */
    // managerId is the employee id of the manager (not the user id)
    // Done by Admin - activate the check after security config
    @PostMapping("/api/employee/add/{managerId}")
    public void insertEmployee(@PathVariable Long managerId,
                               @Valid @RequestBody EmployeeDto employeeDto){
         employeeService.add(managerId, employeeDto);
    }

    @GetMapping("/api/employee/{id}")
    public EmployeeRespDto getById(@PathVariable Long id){
        return employeeService.getById(id);
    }


    @GetMapping("/api/employee/me")
    public EmployeeRespDto getMine(Principal principal){
        return employeeService.getMine(principal.getName());
    }

    @GetMapping("/api/employee/all")
    public List<EmployeeRespDto> getAll(@RequestParam(name = "page" , required = false, defaultValue = "0") Integer page,
                                        @RequestParam(name = "size", required = false, defaultValue = "20") Integer size){
        return employeeService.getAll(page, size);
    }

    // the manager is the logged in user, so no id in the url
    // Done by Manager
    @GetMapping("/api/employee/team")
    public List<EmployeeRespDto> getTeam(Principal principal,
                                         @RequestParam(name = "page" , required = false, defaultValue = "0") Integer page,
                                         @RequestParam(name = "size", required = false, defaultValue = "20") Integer size){
        return employeeService.getTeam(principal.getName(), page, size);
    }

    // department and status are optional - nothing given means all employees
    // Done by Admin / Processor - activate the check after security config
    @GetMapping("/api/employee/filter")
    public List<EmployeeRespDto> dynamicFilterEmployee(@RequestParam(value = "department", required = false) String department,
                                                       @RequestParam(value = "status", required = false) EmployeeStatus status,
                                                       @RequestParam(name = "page" , required = false, defaultValue = "0") Integer page,
                                                       @RequestParam(name = "size", required = false, defaultValue = "20") Integer size){
        return employeeService.filter(department, status, page, size);
    }

    /*
     Department  |  Number of Employees
     Finance         12
     Development     30
    * */
    // Done by Admin - activate the check after security config
    @GetMapping("/api/employee/stat/department")
    public List<DepartmentStatRespDto> getDepartmentStat(){
        return employeeService.getDepartmentStat();
    }

    // Done by Admin - activate the check after security config
    @PutMapping("/api/employee/{id}")
    public void update(@PathVariable Long id,
                       @Valid @RequestBody EmployeeUpdateDto employeeUpdateDto){
        employeeService.update(id, employeeUpdateDto);
    }

    // the employee is the logged in user, so no id in the url
    // Done by Employee / Manager
    @PutMapping("/api/employee/personal")
    public void updatePersonal(@Valid @RequestBody EmployeePersonalUpdateDto employeePersonalUpdateDto,
                               Principal principal){
        employeeService.updatePersonal(principal.getName(), employeePersonalUpdateDto);
    }
}
