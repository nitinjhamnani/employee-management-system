package com.app.service;

import com.app.model.Employee;
import com.app.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmployeeService {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }
    
    public List<Employee> getActiveEmployees() {
        return employeeRepository.findByStatus("ACTIVE");
    }
    
    public List<Employee> getEmployeesByDepartment(String department) {
        return employeeRepository.findByDepartment(department);
    }
    
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }
    
    public Employee saveEmployee(Employee employee) {
        // Generate employee ID (username) for new employees
        if (employee.getId() == null && (employee.getUsername() == null || employee.getUsername().isEmpty())) {
            String employeeId = generateEmployeeId();
            employee.setUsername(employeeId);
            // Set password same as employee ID
            employee.setPassword(passwordEncoder.encode(employeeId));
        } else if (employee.getId() != null) {
            // For existing employees, only encode password if it's being changed and not already encoded
            if (employee.getPassword() != null && !employee.getPassword().isEmpty()) {
                // Check if password is already encoded (starts with $2a$ or $2b$)
                if (!employee.getPassword().startsWith("$2a$") && !employee.getPassword().startsWith("$2b$")) {
                    employee.setPassword(passwordEncoder.encode(employee.getPassword()));
                }
            } else {
                // If password is empty during edit, don't change it - load existing password
                Optional<Employee> existingEmployee = employeeRepository.findById(employee.getId());
                if (existingEmployee.isPresent()) {
                    employee.setPassword(existingEmployee.get().getPassword());
                }
            }
        }
        
        // Set position from hierarchy level if position is not set
        if (employee.getPosition() == null || employee.getPosition().isEmpty()) {
            employee.setPosition(employee.getHierarchyLevel());
        }
        
        return employeeRepository.save(employee);
    }
    
    private String generateEmployeeId() {
        SecureRandom random = new SecureRandom();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder employeeId = new StringBuilder("PG");
        
        // Generate 6 more alphanumeric characters (total 8: PG + 6 chars)
        for (int i = 0; i < 6; i++) {
            employeeId.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        // Ensure uniqueness
        while (employeeRepository.findByUsername(employeeId.toString()) != null) {
            employeeId = new StringBuilder("PG");
            for (int i = 0; i < 6; i++) {
                employeeId.append(chars.charAt(random.nextInt(chars.length())));
            }
        }
        
        return employeeId.toString();
    }
    
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
    
    public List<Employee> searchEmployees(String keyword) {
        return employeeRepository.findByFirstNameContainingOrLastNameContaining(keyword, keyword);
    }
    
    public boolean emailExists(String email) {
        return employeeRepository.findByEmail(email) != null;
    }
    
    public boolean emailExistsForOtherEmployee(String email, Long id) {
        Employee employee = employeeRepository.findByEmail(email);
        return employee != null && !employee.getId().equals(id);
    }
    
    public Optional<Employee> getEmployeeByUsername(String username) {
        Employee employee = employeeRepository.findByUsername(username);
        return Optional.ofNullable(employee);
    }
}

