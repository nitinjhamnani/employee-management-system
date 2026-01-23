package com.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales")
public class Sale {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Employee is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @NotNull(message = "Customer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @NotNull(message = "Sale date is required")
    @Column(nullable = false)
    private LocalDate saleDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    @Column(name = "product_name", nullable = false)
    private String productName;
    
    @Column(length = 1000)
    private String description;
    
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Payment> payments;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal commissionAmount;
    
    @Column(name = "due_date")
    private java.time.LocalDate dueDate;
    
    @NotNull(message = "Quantity is required")
    @Column(nullable = false)
    private Integer quantity = 1;
    
    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, COMPLETED, CANCELLED
    
    @Column(length = 500)
    private String notes;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PrePersist
    protected void onPersist() {
        if (unitPrice != null && quantity != null) {
            totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        // Ensure productName is set
        if ((productName == null || productName.isEmpty()) && product != null && product.getName() != null) {
            productName = product.getName();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (unitPrice != null && quantity != null) {
            totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        // Ensure productName is set
        if ((productName == null || productName.isEmpty()) && product != null && product.getName() != null) {
            productName = product.getName();
        }
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
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public LocalDate getSaleDate() {
        return saleDate;
    }
    
    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public String getProductName() {
        if (productName != null && !productName.isEmpty()) {
            return productName;
        }
        return product != null ? product.getName() : null;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public java.util.List<Payment> getPayments() {
        return payments;
    }
    
    public void setPayments(java.util.List<Payment> payments) {
        this.payments = payments;
    }
    
    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }
    
    public void setCommissionAmount(BigDecimal commissionAmount) {
        this.commissionAmount = commissionAmount;
    }
    
    /**
     * Calculates the total amount paid so far
     */
    public BigDecimal getPaidAmount() {
        try {
            if (payments == null || payments.isEmpty()) {
                return BigDecimal.ZERO;
            }
            return payments.stream()
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            // Handle lazy loading exceptions
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Calculates the remaining amount to be paid
     */
    public BigDecimal getRemainingAmount() {
        try {
            BigDecimal paid = getPaidAmount();
            if (totalAmount == null) {
                return BigDecimal.ZERO;
            }
            return totalAmount.subtract(paid);
        } catch (Exception e) {
            // Handle lazy loading exceptions
            return totalAmount != null ? totalAmount : BigDecimal.ZERO;
        }
    }
    
    /**
     * Checks if the sale is fully paid
     */
    public boolean isFullyPaid() {
        return getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0;
    }
    
    public java.time.LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(java.time.LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        if (this.unitPrice != null && this.quantity != null) {
            this.totalAmount = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        }
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        if (this.unitPrice != null && this.quantity != null) {
            this.totalAmount = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        }
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
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
}

