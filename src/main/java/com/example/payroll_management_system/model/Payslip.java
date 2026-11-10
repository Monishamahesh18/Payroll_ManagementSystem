package com.example.payroll_management_system.model;

import com.example.payroll_management_system.enums.PayStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class Payslip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double gross;

    @Column(nullable = false)
    private Double deduction;

    @Column(nullable = false)
    private Double net;

    @Enumerated(EnumType.STRING)
    private PayStatus payStatus;

    private LocalDate paidOn;

    @ManyToOne
    @JoinColumn(name = "run_id", nullable = false)
    private PayrollRun run;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
