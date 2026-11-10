package com.example.payroll_management_system.model;

import com.example.payroll_management_system.enums.RunStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "payroll_run")
public class PayrollRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    private RunStatus status;

    // reason written when HR sends the run back for reprocess. null for a normal run
    @Column(length = 255)
    private String remarks;

    // both are null for a new run, they get filled in later steps
    @ManyToOne
    @JoinColumn(name = "processed_by")
    private User processedBy;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;
}
