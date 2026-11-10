package com.example.payroll_management_system.model;

import com.example.payroll_management_system.enums.ComponentType;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class PayslipItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private ComponentType type;

    @Column(nullable = false)
    private Double amount;

    private boolean isStatutory;

    @ManyToOne
    @JoinColumn(name = "payslip_id", nullable = false)
    private Payslip payslip;
}