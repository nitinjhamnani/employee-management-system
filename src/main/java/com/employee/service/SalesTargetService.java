package com.employee.service;

import com.employee.model.SalesTarget;
import com.employee.model.Employee;
import com.employee.model.Sale;
import com.employee.repository.SalesTargetRepository;
import com.employee.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SalesTargetService {
    
    @Autowired
    private SalesTargetRepository salesTargetRepository;
    
    @Autowired
    private SaleRepository saleRepository;
    
    public List<SalesTarget> getSalesTargetsByEmployee(Employee employee) {
        return salesTargetRepository.findByEmployeeOrderByPeriodStartDesc(employee);
    }
    
    public Optional<SalesTarget> getCurrentSalesTarget(Employee employee) {
        LocalDate today = LocalDate.now();
        return salesTargetRepository.findByEmployeeAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(
                employee, today, today);
    }
    
    public Optional<SalesTarget> getSalesTargetById(Long id) {
        return salesTargetRepository.findById(id);
    }
    
    public SalesTarget saveSalesTarget(SalesTarget salesTarget) {
        return salesTargetRepository.save(salesTarget);
    }
    
    public void deleteSalesTarget(Long id) {
        salesTargetRepository.deleteById(id);
    }
    
    public void updateAchievedAmount(SalesTarget salesTarget) {
        LocalDate startDate = salesTarget.getPeriodStart();
        LocalDate endDate = salesTarget.getPeriodEnd();
        
        List<Sale> sales = saleRepository.findByEmployeeAndSaleDateBetween(
                salesTarget.getEmployee(), startDate, endDate);
        
        BigDecimal total = sales.stream()
                .filter(s -> "COMPLETED".equals(s.getStatus()))
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        salesTarget.setAchievedAmount(total);
        salesTargetRepository.save(salesTarget);
    }
    
    public void updateAllAchievedAmounts() {
        List<SalesTarget> targets = salesTargetRepository.findAll();
        for (SalesTarget target : targets) {
            updateAchievedAmount(target);
        }
    }
}

