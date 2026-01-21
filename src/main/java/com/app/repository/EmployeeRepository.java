package com.app.repository;

import com.app.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByStatus(String status);
    List<Employee> findByDepartment(String department);
    Employee findByEmail(String email);
    Employee findByUsername(String username);
    List<Employee> findByFirstNameContainingOrLastNameContaining(String firstName, String lastName);
}

