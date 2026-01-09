package com.employee.repository;

import com.employee.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByStatus(String status);
    List<Customer> findByCompanyNameContaining(String companyName);
    Customer findByEmail(String email);
    List<Customer> findByIndustry(String industry);
}

