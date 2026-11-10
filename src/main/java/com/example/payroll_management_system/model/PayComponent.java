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
@Table(name = "pay_component")
public class PayComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private ComponentType type;

    @Column(nullable = false)
    private Double value;

    private boolean isStatutory;

    // false = this rule is retired and payroll should skip it
    private boolean isActive = true;
}
