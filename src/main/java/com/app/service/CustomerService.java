package com.app.service;

import com.app.model.Customer;
import com.app.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustomerService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    public List<Customer> getActiveCustomers() {
        return customerRepository.findByStatus("ACTIVE");
    }
    
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }
    
    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }
    
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
    
    public List<Customer> searchCustomers(String keyword) {
        List<Customer> results = customerRepository.findByContactPersonContainingIgnoreCase(keyword);
        results.addAll(customerRepository.findByEmailContainingIgnoreCase(keyword));
        results.addAll(customerRepository.findByPhoneContaining(keyword));
        return results.stream().distinct().toList();
    }
}

