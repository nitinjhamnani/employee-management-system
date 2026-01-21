package com.app.repository;

import com.app.model.Sale;
import com.app.model.Employee;
import com.app.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByEmployee(Employee employee);
    List<Sale> findByCustomer(Customer customer);
    List<Sale> findBySaleDate(LocalDate saleDate);
    List<Sale> findBySaleDateBetween(LocalDate startDate, LocalDate endDate);
    List<Sale> findByStatus(String status);
    List<Sale> findByEmployeeAndSaleDateBetween(Employee employee, LocalDate startDate, LocalDate endDate);
}

