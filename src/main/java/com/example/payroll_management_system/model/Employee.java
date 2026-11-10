package com.example.payroll_management_system.model;

import com.example.payroll_management_system.enums.EmployeeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String empCode;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private LocalDate joinDate;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    @Column(nullable = false)
    private Double basic;

    @Column(nullable = false)
    private Double hra;

    @Column(nullable = false)
    private Double allowance;

    @Column(length = 30)
    private String bankAcc;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String address;

    // tax withholding in percent. starts as 0 so new employee can save without it
    @Column(nullable = false)
    private Double taxWithholding = 0.0;

    // one login (user) for one employee
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // manager is also an employee. for the top manager this will be null
    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Employee manager;
}
