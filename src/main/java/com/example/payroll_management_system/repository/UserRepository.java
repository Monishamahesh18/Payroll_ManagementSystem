package com.example.payroll_management_system.repository;

import com.example.payroll_management_system.dto.responseDto.DetailsDto;
import com.example.payroll_management_system.dto.responseDto.UserRespDto;
import com.example.payroll_management_system.enums.Role;
import com.example.payroll_management_system.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // role is optional - null gives the users of all roles
    @Query("""
        select new com.example.payroll_management_system.dto.responseDto.UserRespDto(
        u.id,
        u.email,
        u.role,
        u.isActive
        )
        from User u
        where (?1 IS NULL OR u.role = ?1)
        order by u.id
        """)
    List<UserRespDto> getUsersByRole(Role role, Pageable pageable);

    List<User> findByRole(Role role);

    @Query("select count(u) from User u where u.isActive = ?1")
    long countUsersByActive(boolean isActive);

    @Query("select count(u) from User u where u.role = ?1 AND u.isActive = true")
    long countActiveUsersByRole(Role role);

    @Query("""
            select u
            from User u
            where u.email=?1 AND u.isActive = ?2
            """)
    User loadUserByUsername(String email,boolean isActive);

    @Query("""
            select new com.example.payroll_management_system.dto.responseDto.DetailsDto(u.email, u.isActive, u.role)
            from User u
            where u.email=?1
            """)
    DetailsDto getUserDetails(String loggedInUsername);
}
