package com.app.repository;

import com.app.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByStatus(String status);
    Customer findByEmail(String email);
    Customer findByEmailAndPhone(String email, String phone);
    List<Customer> findByContactPersonContainingIgnoreCase(String contactPerson);
    List<Customer> findByEmailContainingIgnoreCase(String email);
    List<Customer> findByPhoneContaining(String phone);
    List<Customer> findByPromoterId(Long promoterId);
    List<Customer> findByZonalHeadId(Long zonalHeadId);
    List<Customer> findByClusterHeadId(Long clusterHeadId);
    List<Customer> findByAreaSalesManagerId(Long areaSalesManagerId);
}

