package com.example.payroll_management_system.repository;

import com.example.payroll_management_system.enums.ComponentType;
import com.example.payroll_management_system.model.PayComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaycomponentRepository extends JpaRepository<PayComponent, Long> {

    List<PayComponent> findByType(ComponentType type);

    // payroll uses only the active components
    @Query("select p from PayComponent p where p.type = ?1 AND p.isActive = true")
    List<PayComponent> getActiveByType(ComponentType type);

    @Query("select count(p) from PayComponent p where p.isActive = true")
    long countActive();
}
