package com.example.payroll_management_system.service;

import com.example.payroll_management_system.dto.requestDto.EmployeeDto;
import com.example.payroll_management_system.dto.requestDto.EmployeePersonalUpdateDto;
import com.example.payroll_management_system.dto.requestDto.EmployeeUpdateDto;
import com.example.payroll_management_system.dto.responseDto.DepartmentStatRespDto;
import com.example.payroll_management_system.dto.responseDto.EmployeeRespDto;
import com.example.payroll_management_system.enums.EmployeeStatus;
import com.example.payroll_management_system.enums.Role;
import com.example.payroll_management_system.exceptions.InvalidCallException;
import com.example.payroll_management_system.exceptions.ResourceNotFoundException;
import com.example.payroll_management_system.mapper.EmployeeMapper;
import com.example.payroll_management_system.model.Employee;
import com.example.payroll_management_system.model.User;
import com.example.payroll_management_system.repository.EmployeeRepository;
import com.example.payroll_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    // manager is also an employee. there is no manager above him, so manager column stays null
    // @Transactional: if employee save fails, the user will also be rolled back (no half data in DB)
    @Transactional
    public void addManager(EmployeeDto employeeDto) {
        // Step 1: first save the User in DB (email, password, role MANAGER)
        // user should go in DB first and then only employee can save, because employee table needs user_id
        User user = userService.getUserObj(
                employeeDto.email(),
                employeeDto.password(),
                Role.MANAGER
        );

        // Step 2: change EmployeeDto to Employee entity using mapper
        Employee employee = EmployeeMapper.convertDtoToEntity(employeeDto);

        // Step 3: attach the user to employee
        employee.setUser(user);

        // Step 3.5: new employee always starts with ONBOARDING status
        employee.setStatus(EmployeeStatus.ONBOARDING);

        // Step 4: save employee in DB
        employeeRepository.save(employee);
        log.info("Manager added: {}", employeeDto.email());
    }

    @Transactional
    public void add(Long managerId, EmployeeDto employeeDto) {
        // Step 0: get the manager from DB using the id given in url
        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(()-> new ResourceNotFoundException("Manager id Invalid"));

        // Step 0.5: the id should belong to a manager only, not to a normal employee
        if(manager.getUser().getRole() != Role.MANAGER)
            throw new InvalidCallException("Given id does not belong to a manager");

        // Step 1: first save the User in DB (email, password, role EMPLOYEE)
        User user = userService.getUserObj(
                employeeDto.email(),
                employeeDto.password(),
                Role.EMPLOYEE
        );

        // Step 2: change EmployeeDto to Employee entity using mapper
        Employee employee = EmployeeMapper.convertDtoToEntity(employeeDto);

        // Step 3: attach the user and the manager to employee
        employee.setUser(user);
        employee.setManager(manager);

        // Step 3.5: new employee always starts with ONBOARDING status
        employee.setStatus(EmployeeStatus.ONBOARDING);

        // Step 4: save employee in DB
        employeeRepository.save(employee);
        log.info("Employee {} added under manager {}", employeeDto.email(), managerId);
    }

    public EmployeeRespDto getById(Long id) {
        // Step 1: get the employee from DB using the id given in url
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Employee Not Found"));

        // Step 2: change Employee entity to EmployeeRespDto using mapper
        return EmployeeMapper.convertEntityToDto(employee);
    }

    public List<EmployeeRespDto> getAll(Integer page, Integer size) {
        // Step 1: create Pageable using page and size values thru PageRequest
        Pageable pageable = PageRequest.of(page, size);

        // Step 2: get the employees of that page from DB
        List<Employee> list = employeeRepository.findAll(pageable).getContent();

        // Step 3: Map List<Employee> to List<EmployeeRespDto> using Mapper
        return list
                .stream()
                .map(EmployeeMapper :: convertEntityToDto)
                .toList();
    }

    // the manager is the logged in user, so the email comes from the token
    public List<EmployeeRespDto> getTeam(String managerEmail, Integer page, Integer size) {
        // Step 1: get the logged in manager from DB using the email
        Employee manager = employeeRepository.getByUserEmail(managerEmail);
        if(manager == null)
            throw new ResourceNotFoundException("Manager Id Invalid");

        // Step 2: create Pageable using page and size values thru PageRequest
        Pageable pageable = PageRequest.of(page, size);

        // Step 3: get the team of this manager (query gives the dto directly)
        return employeeRepository.getTeamByManagerId(manager.getId(), pageable);
    }

    public void update(Long id, EmployeeUpdateDto dto) {
        // Step 1: get the employee from DB using the id given in url
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Employee Not Found"));

        // Step 2: update only the fields that are sent
        boolean flag = false;

        if(!(dto.fullName() == null) && !(dto.fullName().trim().isEmpty())) {
            employee.setFullName(dto.fullName());
            flag = true;
        }

        if(!(dto.department() == null) && !(dto.department().trim().isEmpty())) {
            employee.setDepartment(dto.department());
            flag = true;
        }

        if(!(dto.basic() == null)) {
            employee.setBasic(dto.basic());
            flag = true;
        }

        if(!(dto.hra() == null)) {
            employee.setHra(dto.hra());
            flag = true;
        }

        if(!(dto.allowance() == null)) {
            employee.setAllowance(dto.allowance());
            flag = true;
        }

        if(!(dto.bankAcc() == null) && !(dto.bankAcc().trim().isEmpty())) {
            employee.setBankAcc(dto.bankAcc());
            flag = true;
        }

        // Step 3: nothing was sent to update
        if(!flag)
            throw new InvalidCallException("Update op terminated");

        // Step 4: save in DB
        employeeRepository.save(employee);
        log.info("Details of employee {} are updated", id);
    }

    // employee updates own personal details (no salary fields here)
    public void updatePersonal(String email, EmployeePersonalUpdateDto dto) {
        // Step 1: get the logged in employee from DB using the email of the token
        Employee employee = employeeRepository.getByUserEmail(email);
        if(employee == null)
            throw new ResourceNotFoundException("Employee Not Found");

        // Step 2: update only the fields that are sent
        boolean flag = false;

        if(!(dto.phone() == null) && !(dto.phone().trim().isEmpty())) {
            employee.setPhone(dto.phone());
            flag = true;
        }

        if(!(dto.address() == null) && !(dto.address().trim().isEmpty())) {
            employee.setAddress(dto.address());
            flag = true;
        }

        if(!(dto.bankAcc() == null) && !(dto.bankAcc().trim().isEmpty())) {
            employee.setBankAcc(dto.bankAcc());
            flag = true;
        }

        if(!(dto.taxWithholding() == null)) {
            // tax withholding is a percent, so it should be from 0 to 100
            if(dto.taxWithholding() > 100)
                throw new InvalidCallException("Tax withholding cannot be more than 100 percent");
            employee.setTaxWithholding(dto.taxWithholding());
            flag = true;
        }

        // Step 3: nothing was sent to update
        if(!flag)
            throw new InvalidCallException("Update op terminated");

        // Step 4: save in DB
        employeeRepository.save(employee);
        log.info("{} updated his personal details", email);
    }

    @Transactional
    public void updateStatus(long employeeId, EmployeeStatus status) {
        // Step 1: get the employee from DB using employeeId
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(()-> new ResourceNotFoundException("Employee Not Found"));

        // Step 2: same status is given again
        if(employee.getStatus() == status)
            throw new InvalidCallException("Employee is already " + status);

        // Step 3: retired employee cannot login. if he comes back from RETIRED, login works again
        User user = employee.getUser();
        if(status == EmployeeStatus.RETIRED)
            user.setActive(false);
        else if(employee.getStatus() == EmployeeStatus.RETIRED)
            user.setActive(true);

        // Step 4: update status and save both
        employee.setStatus(status);
        userRepository.save(user);
        employeeRepository.save(employee);
        log.info("Status of employee {} is now {}", employeeId, status);
    }

    // department and status are optional - null gives all
    public List<EmployeeRespDto> filter(String department, EmployeeStatus status, Integer page, Integer size) {
        // Step 1: create Pageable using page and size values thru PageRequest
        Pageable pageable = PageRequest.of(page, size);

        // Step 2: blank department is treated as not given
        String departmentFilter = (department == null || department.trim().isEmpty()) ? null : department.trim();

        // Step 3: get the employees (query gives the dto directly)
        return employeeRepository.dynamicFilterEmployee(departmentFilter, status, pageable);
    }

    // Department | Number of Employees
    public List<DepartmentStatRespDto> getDepartmentStat() {
        return employeeRepository.getDepartmentStat();
    }

    public EmployeeRespDto getMine(String email) {
        // Step 1: get the logged in employee from DB using the email of the token
        Employee employee = employeeRepository.getByUserEmail(email);
        if(employee == null)
            throw new ResourceNotFoundException("Employee Not Found");

        // Step 2: change Employee entity to EmployeeRespDto using mapper
        return EmployeeMapper.convertEntityToDto(employee);
    }
}
