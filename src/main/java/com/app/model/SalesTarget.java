package com.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_targets")
public class SalesTarget {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Employee is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @NotNull(message = "Target amount is required")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal targetAmount;
    
    @NotNull(message = "Period start date is required")
    @Column(nullable = false)
    private LocalDate periodStart;
    
    @NotNull(message = "Period end date is required")
    @Column(nullable = false)
    private LocalDate periodEnd;
    
    @Column(precision = 12, scale = 2)
    private BigDecimal achievedAmount = BigDecimal.ZERO;
    
    @Column(precision = 12, scale = 2)
    private BigDecimal baseSalary;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal commissionRate = BigDecimal.ZERO; // Commission percentage (e.g., 5.00 for 5%)
    
    @Column(precision = 12, scale = 2)
    private BigDecimal calculatedSalary;
    
    @Column
    private Boolean salaryCalculated = false;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Employee getEmployee() {
        return employee;
    }
    
    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
    
    public BigDecimal getTargetAmount() {
        return targetAmount;
    }
    
    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }
    
    public LocalDate getPeriodStart() {
        return periodStart;
    }
    
    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }
    
    public LocalDate getPeriodEnd() {
        return periodEnd;
    }
    
    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }
    
    public BigDecimal getAchievedAmount() {
        return achievedAmount;
    }
    
    public void setAchievedAmount(BigDecimal achievedAmount) {
        this.achievedAmount = achievedAmount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public BigDecimal getProgressPercentage() {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return achievedAmount.divide(targetAmount, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    public BigDecimal getBaseSalary() {
        return baseSalary;
    }
    
    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }
    
    public BigDecimal getCommissionRate() {
        return commissionRate;
    }
    
    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }
    
    public BigDecimal getCalculatedSalary() {
        return calculatedSalary;
    }
    
    public void setCalculatedSalary(BigDecimal calculatedSalary) {
        this.calculatedSalary = calculatedSalary;
    }
    
    public Boolean getSalaryCalculated() {
        return salaryCalculated;
    }
    
    public void setSalaryCalculated(Boolean salaryCalculated) {
        this.salaryCalculated = salaryCalculated;
    }
}

