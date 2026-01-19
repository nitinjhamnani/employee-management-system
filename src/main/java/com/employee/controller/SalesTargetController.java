package com.employee.controller;

import com.employee.model.SalesTarget;
import com.employee.model.Employee;
import com.employee.service.SalesTargetService;
import com.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/sales-targets")
public class SalesTargetController {
    
    @Autowired
    private SalesTargetService salesTargetService;
    
    @Autowired
    private EmployeeService employeeService;
    
    @GetMapping
    public String listSalesTargets(Model model,
                                   @RequestParam(required = false) Long employeeId,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
        List<SalesTarget> targets;
        
        if (employeeId != null) {
            Employee employee = employeeService.getEmployeeById(employeeId)
                    .orElse(null);
            if (employee != null) {
                targets = salesTargetService.getSalesTargetsByEmployee(employee);
                model.addAttribute("selectedEmployeeId", employeeId);
            } else {
                targets = salesTargetService.getAllSalesTargets();
            }
        } else if (month != null) {
            // Filter by month
            LocalDate startOfMonth = month.withDayOfMonth(1);
            LocalDate endOfMonth = month.withDayOfMonth(month.lengthOfMonth());
            targets = salesTargetService.getSalesTargetsByDateRange(startOfMonth, endOfMonth);
            model.addAttribute("selectedMonth", month);
        } else {
            targets = salesTargetService.getAllSalesTargets();
        }
        
        model.addAttribute("targets", targets);
        model.addAttribute("employees", employeeService.getActiveEmployees());
        return "admin/sales-targets/list";
    }
    
    @GetMapping("/new")
    public String showSalesTargetForm(Model model) {
        SalesTarget salesTarget = new SalesTarget();
        // Set default period to current month
        LocalDate now = LocalDate.now();
        salesTarget.setPeriodStart(now.withDayOfMonth(1));
        salesTarget.setPeriodEnd(now.withDayOfMonth(now.lengthOfMonth()));
        model.addAttribute("salesTarget", salesTarget);
        model.addAttribute("employees", employeeService.getActiveEmployees());
        return "admin/sales-targets/form";
    }
    
    @GetMapping("/edit/{id}")
    public String editSalesTarget(@PathVariable Long id, Model model) {
        SalesTarget salesTarget = salesTargetService.getSalesTargetById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid sales target ID: " + id));
        model.addAttribute("salesTarget", salesTarget);
        model.addAttribute("employees", employeeService.getActiveEmployees());
        return "admin/sales-targets/form";
    }
    
    @PostMapping("/save")
    public String saveSalesTarget(@Valid @ModelAttribute SalesTarget salesTarget,
                                 @RequestParam(required = false) Long employeeId,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("employees", employeeService.getActiveEmployees());
            return "admin/sales-targets/form";
        }
        
        if (employeeId != null) {
            Employee employee = employeeService.getEmployeeById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid employee ID: " + employeeId));
            salesTarget.setEmployee(employee);
        }
        
        // If base salary is not set, use employee's current salary
        if (salesTarget.getBaseSalary() == null && salesTarget.getEmployee() != null) {
            if (salesTarget.getEmployee().getSalary() != null) {
                salesTarget.setBaseSalary(BigDecimal.valueOf(salesTarget.getEmployee().getSalary()));
            }
        }
        
        salesTargetService.saveSalesTarget(salesTarget);
        
        // Update achieved amount
        salesTargetService.updateAchievedAmount(salesTarget);
        
        redirectAttributes.addFlashAttribute("message", "Sales target saved successfully!");
        return "redirect:/admin/sales-targets";
    }
    
    @GetMapping("/delete/{id}")
    public String deleteSalesTarget(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        salesTargetService.deleteSalesTarget(id);
        redirectAttributes.addFlashAttribute("message", "Sales target deleted successfully!");
        return "redirect:/admin/sales-targets";
    }
    
    @PostMapping("/calculate-salary/{id}")
    public String calculateSalary(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            SalesTarget salesTarget = salesTargetService.getSalesTargetById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid sales target ID: " + id));
            
            // Update achieved amount first
            salesTargetService.updateAchievedAmount(salesTarget);
            
            // Calculate and update salary
            salesTargetService.calculateAndUpdateSalary(salesTarget);
            
            redirectAttributes.addFlashAttribute("message", "Salary calculated and updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error calculating salary: " + e.getMessage());
        }
        return "redirect:/admin/sales-targets";
    }
    
    @PostMapping("/calculate-all-salaries")
    public String calculateAllSalaries(RedirectAttributes redirectAttributes) {
        try {
            salesTargetService.recalculateAllSalaries();
            redirectAttributes.addFlashAttribute("message", "All salaries calculated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error calculating salaries: " + e.getMessage());
        }
        return "redirect:/admin/sales-targets";
    }
    
    @GetMapping("/view/{id}")
    public String viewSalesTarget(@PathVariable Long id, Model model) {
        SalesTarget salesTarget = salesTargetService.getSalesTargetById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid sales target ID: " + id));
        model.addAttribute("salesTarget", salesTarget);
        return "admin/sales-targets/view";
    }
}
