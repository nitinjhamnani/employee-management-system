package com.employee.repository;

import com.employee.model.Task;
import com.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByEmployee(Employee employee);
    List<Task> findByEmployeeAndStatus(Employee employee, String status);
    List<Task> findByEmployeeOrderByCreatedAtDesc(Employee employee);
}

