package com.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Contact person is required")
    @Column(nullable = false)
    private String contactPerson;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false)
    private String email;
    
    @NotBlank(message = "Phone is required")
    @Column(nullable = false)
    private String phone;
    
    @Column(length = 500)
    private String address;
    
    @Column(length = 100)
    private String area;
    
    @Column(length = 100)
    private String city;
    
    @Column(length = 100)
    private String state;
    
    @Column(length = 20)
    private String zipCode;
    
    @Column(length = 100)
    private String country;
    
    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, POTENTIAL
    
    @Column(length = 1000)
    private String notes;
    
    @Column(name = "promoter_id")
    private Long promoterId;
    
    @Column(name = "zonal_head_id")
    private Long zonalHeadId;
    
    @Column(name = "cluster_head_id")
    private Long clusterHeadId;
    
    @Column(name = "area_sales_manager_id")
    private Long areaSalesManagerId;
    
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
    
    public String getContactPerson() {
        return contactPerson;
    }
    
    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getArea() {
        return area;
    }
    
    public void setArea(String area) {
        this.area = area;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getZipCode() {
        return zipCode;
    }
    
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
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
    
    public Long getPromoterId() {
        return promoterId;
    }
    
    public void setPromoterId(Long promoterId) {
        this.promoterId = promoterId;
    }
    
    public Long getZonalHeadId() {
        return zonalHeadId;
    }
    
    public void setZonalHeadId(Long zonalHeadId) {
        this.zonalHeadId = zonalHeadId;
    }
    
    public Long getClusterHeadId() {
        return clusterHeadId;
    }
    
    public void setClusterHeadId(Long clusterHeadId) {
        this.clusterHeadId = clusterHeadId;
    }
    
    public Long getAreaSalesManagerId() {
        return areaSalesManagerId;
    }
    
    public void setAreaSalesManagerId(Long areaSalesManagerId) {
        this.areaSalesManagerId = areaSalesManagerId;
    }
}

