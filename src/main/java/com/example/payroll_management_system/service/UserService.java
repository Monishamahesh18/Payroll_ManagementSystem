package com.example.payroll_management_system.service;

import com.example.payroll_management_system.dto.requestDto.AdminDto;
import com.example.payroll_management_system.dto.requestDto.PasswordDto;
import com.example.payroll_management_system.dto.responseDto.DetailsDto;
import com.example.payroll_management_system.dto.responseDto.UserRespDto;
import com.example.payroll_management_system.enums.Role;
import com.example.payroll_management_system.exceptions.InvalidCallException;
import com.example.payroll_management_system.exceptions.ResourceNotFoundException;
import com.example.payroll_management_system.mapper.AdminMapper;
import com.example.payroll_management_system.model.Employee;
import com.example.payroll_management_system.model.User;
import com.example.payroll_management_system.repository.EmployeeRepository;
import com.example.payroll_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    // used by manager and employee. it saves the user in DB and gives back the saved user (with id)
    public User getUserObj(String email, String password, Role role){
        User user = new User(email, passwordEncoder.encode(password), role);
        return userRepository.save(user);
    }

    public void addAdmin(AdminDto adminDto) {
        // Step 1: Prepare user obj using mapper
        User user = AdminMapper.convertDtoToEntity(adminDto);
        user.setRole(Role.ADMIN);

        // Step 1.5 : Encode the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Step 2: Save user in DB
        userRepository.save(user);
        log.info("Admin created: {}", adminDto.email());
    }

    public void addProcessor(AdminDto adminDto) {
        // Step 1: change AdminDto to User entity using mapper (same dto and mapper as admin)
        User user = AdminMapper.convertDtoToEntity(adminDto);

        // Step 2: this is processor, so set role as PROCESSOR and encode the password
        user.setRole(Role.PROCESSOR);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Step 3: save user in DB
        userRepository.save(user);
        log.info("Processor created: {}", adminDto.email());
    }

    // role is optional - null gives the users of all roles
    public List<UserRespDto> getAll(Role role, Integer page, Integer size) {
        // Step 1: create Pageable using page and size values thru PageRequest
        Pageable pageable = PageRequest.of(page, size);

        // Step 2: get the users (query gives the dto directly)
        return userRepository.getUsersByRole(role, pageable);
    }

    public void deactivate(long userId) {
        // Step 1: get the user from DB using userId
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User Not Found"));

        // Step 2: already inactive
        if(!user.isActive())
            throw new InvalidCallException("User is already deactivated");

        // Step 3: the last active admin cannot be deactivated, else nobody can manage the system
        checkLastAdmin(user);

        // Step 4: mark user as inactive and save
        user.setActive(false);
        userRepository.save(user);
        log.info("Login of user {} is deactivated", userId);
    }

    public void activate(long userId) {
        // Step 1: get the user from DB using userId
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User Not Found"));

        // Step 2: already active
        if(user.isActive())
            throw new InvalidCallException("User is already active");

        // Step 3: mark user as active and save
        user.setActive(true);
        userRepository.save(user);
        log.info("Login of user {} is activated", userId);
    }

    @Transactional
    public void changeRole(long userId, Role role) {
        // Step 1: get the user from DB using userId
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User Not Found"));

        // Step 2: same role is given again
        if(user.getRole() == role)
            throw new InvalidCallException("User already has this role");

        // Step 3: the last active admin cannot be changed
        checkLastAdmin(user);

        // Step 4: employee users can be MANAGER or EMPLOYEE only. admin and processor have no employee record
        Employee employee = employeeRepository.getByUserId(userId);
        if(employee != null && (role == Role.ADMIN || role == Role.PROCESSOR))
            throw new InvalidCallException("This user is an employee, role can be MANAGER or EMPLOYEE only");
        if(employee == null && (role == Role.MANAGER || role == Role.EMPLOYEE))
            throw new InvalidCallException("This user has no employee record, role can be ADMIN or PROCESSOR only");

        // Step 5: a manager who still has team members cannot become a normal employee
        if(employee != null && user.getRole() == Role.MANAGER && employeeRepository.countTeam(employee.getId()) > 0)
            throw new InvalidCallException("This manager still has team members, so role cannot be changed");

        // Step 6: set the new role and save
        user.setRole(role);
        userRepository.save(user);
        log.info("Role of user {} is changed to {}", userId, role);
    }

    public void changePassword(long userId, PasswordDto passwordDto) {
        // Step 1: get the user from DB using userId
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User Not Found"));

        // Step 2: encode the new password and save
        user.setPassword(passwordEncoder.encode(passwordDto.password()));
        userRepository.save(user);
        log.info("Password of user {} is reset", userId);
    }

    private void checkLastAdmin(User user) {
        if(user.getRole() == Role.ADMIN && user.isActive()
                && userRepository.countActiveUsersByRole(Role.ADMIN) <= 1)
            throw new InvalidCallException("Last active admin cannot be deactivated or changed");
    }

    public DetailsDto getUserDetails(String loggedInUsername) {
        return userRepository.getUserDetails(loggedInUsername);
    }
}
