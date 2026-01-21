package com.app.repository;

import com.app.model.SalesTarget;
import com.app.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesTargetRepository extends JpaRepository<SalesTarget, Long> {
    List<SalesTarget> findByEmployee(Employee employee);
    Optional<SalesTarget> findByEmployeeAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(
            Employee employee, LocalDate date1, LocalDate date2);
    List<SalesTarget> findByEmployeeOrderByPeriodStartDesc(Employee employee);
}

