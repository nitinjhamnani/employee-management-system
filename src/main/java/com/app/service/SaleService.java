package com.app.service;

import com.app.model.Sale;
import com.app.model.Employee;
import com.app.model.Customer;
import com.app.repository.SaleRepository;
import com.app.repository.EmployeeRepository;
import com.app.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SaleService {
    
    @Autowired
    private SaleRepository saleRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }
    
    public List<Sale> getSalesByEmployee(Long employeeId) {
        Optional<Employee> employee = employeeRepository.findById(employeeId);
        return employee.map(saleRepository::findByEmployee).orElse(List.of());
    }
    
    public List<Sale> getSalesByCustomer(Long customerId) {
        Optional<Customer> customer = customerRepository.findById(customerId);
        return customer.map(saleRepository::findByCustomer).orElse(List.of());
    }
    
    public List<Sale> getSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        return saleRepository.findBySaleDateBetween(startDate, endDate);
    }
    
    public Optional<Sale> getSaleById(Long id) {
        return saleRepository.findById(id);
    }
    
    public Sale saveSale(Sale sale) {
        if (sale.getUnitPrice() != null && sale.getQuantity() != null) {
            sale.setTotalAmount(sale.getUnitPrice().multiply(BigDecimal.valueOf(sale.getQuantity())));
        }
        return saleRepository.save(sale);
    }
    
    public void deleteSale(Long id) {
        saleRepository.deleteById(id);
    }
    
    public BigDecimal getTotalSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Sale> sales = saleRepository.findBySaleDateBetween(startDate, endDate);
        return sales.stream()
                   .filter(s -> "COMPLETED".equals(s.getStatus()))
                   .map(Sale::getTotalAmount)
                   .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getTotalSalesByEmployee(Long employeeId, LocalDate startDate, LocalDate endDate) {
        Optional<Employee> employee = employeeRepository.findById(employeeId);
        if (employee.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        List<Sale> sales = saleRepository.findByEmployeeAndSaleDateBetween(employee.get(), startDate, endDate);
        return sales.stream()
                   .filter(s -> "COMPLETED".equals(s.getStatus()))
                   .map(Sale::getTotalAmount)
                   .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public List<Sale> getSalesByEmployeeAndDateRange(Long employeeId, LocalDate startDate, LocalDate endDate) {
        Optional<Employee> employee = employeeRepository.findById(employeeId);
        if (employee.isEmpty()) {
            return List.of();
        }
        return saleRepository.findByEmployeeAndSaleDateBetween(employee.get(), startDate, endDate);
    }
}

