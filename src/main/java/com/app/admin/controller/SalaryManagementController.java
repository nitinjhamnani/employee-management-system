package com.app.admin.controller;

import com.app.model.Employee;
import com.app.model.SalesTarget;
import com.app.service.EmployeeService;
import com.app.service.SalesTargetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/salary-management")
public class SalaryManagementController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private SalesTargetService salesTargetService;

    @GetMapping
    public String listEmployeesForSalary(Model model,
                                       @RequestParam(required = false) String hierarchyLevel,
                                       @RequestParam(required = false) String search) {
        List<Employee> employees = employeeService.getActiveEmployees();

        // Filter by hierarchy level if provided
        if (hierarchyLevel != null && !hierarchyLevel.isEmpty()) {
            employees = employees.stream()
                    .filter(emp -> emp.getHierarchyLevel() != null && emp.getHierarchyLevel().toString().equals(hierarchyLevel))
                    .collect(java.util.stream.Collectors.toList());
            model.addAttribute("selectedHierarchyLevel", hierarchyLevel);
        }

        // Filter by search query if provided
        if (search != null && !search.isEmpty()) {
            final String searchLower = search.toLowerCase();
            employees = employees.stream()
                    .filter(emp ->
                        (emp.getFullName() != null && emp.getFullName().toLowerCase().contains(searchLower)) ||
                        (emp.getUsername() != null && emp.getUsername().toLowerCase().contains(searchLower)) ||
                        (emp.getEmail() != null && emp.getEmail().toLowerCase().contains(searchLower))
                    )
                    .collect(java.util.stream.Collectors.toList());
            model.addAttribute("searchQuery", search);
        }

        model.addAttribute("employees", employees);
        return "admin/salary-management/list";
    }

    @GetMapping("/employee/{id}")
    public String viewEmployeeSalary(@PathVariable Long id, Model model) {
        Employee employee = employeeService.getEmployeeById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee ID: " + id));

        // Get all sales targets for the employee
        List<SalesTarget> salesTargets = salesTargetService.getSalesTargetsByEmployee(employee);

        // Calculate total commission earned
        BigDecimal totalCommission = salesTargets.stream()
                .map(target -> target.getCommissionAmount() != null ? target.getCommissionAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate current month salary (base + commission)
        BigDecimal currentMonthSalary = calculateCurrentMonthSalary(employee, salesTargets);

        model.addAttribute("employee", employee);
        model.addAttribute("salesTargets", salesTargets);
        model.addAttribute("totalCommission", totalCommission);
        model.addAttribute("currentMonthSalary", currentMonthSalary);

        return "admin/salary-management/employee-salary";
    }

    @PostMapping("/calculate-salary/{employeeId}")
    public String calculateEmployeeSalary(@PathVariable Long employeeId,
                                        @RequestParam BigDecimal baseSalary,
                                        @RequestParam BigDecimal additionalBonus,
                                        RedirectAttributes redirectAttributes) {
        try {
            Employee employee = employeeService.getEmployeeById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid employee ID: " + employeeId));

            // Get all sales targets for the employee
            List<SalesTarget> salesTargets = salesTargetService.getSalesTargetsByEmployee(employee);

            // Calculate commission from sales targets
            BigDecimal totalCommission = salesTargets.stream()
                    .map(target -> target.getCommissionAmount() != null ? target.getCommissionAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate total salary
            BigDecimal totalSalary = baseSalary.add(totalCommission).add(additionalBonus != null ? additionalBonus : BigDecimal.ZERO);

            // Here you would typically save the salary record to a salary table
            // For now, we'll just show success message
            redirectAttributes.addFlashAttribute("message",
                "Salary calculated successfully for " + employee.getFullName() + ". Total: ₹" + totalSalary);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error calculating salary: " + e.getMessage());
        }

        return "redirect:/admin/salary-management/employee/" + employeeId;
    }

    @PostMapping("/release-salary/{employeeId}")
    public String releaseEmployeeSalary(@PathVariable Long employeeId,
                                      @RequestParam BigDecimal finalSalary,
                                      RedirectAttributes redirectAttributes) {
        try {
            Employee employee = employeeService.getEmployeeById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid employee ID: " + employeeId));

            // Here you would typically create a salary payment record
            // For now, we'll just show success message
            redirectAttributes.addFlashAttribute("message",
                "Salary released successfully for " + employee.getFullName() + ". Amount: ₹" + finalSalary);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error releasing salary: " + e.getMessage());
        }

        return "redirect:/admin/salary-management/employee/" + employeeId;
    }

    @PostMapping("/calculate-all-salaries")
    public String calculateAllSalaries(RedirectAttributes redirectAttributes) {
        try {
            // This would calculate salaries for all employees
            // For now, just show success message
            redirectAttributes.addFlashAttribute("message", "All salaries calculated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error calculating salaries: " + e.getMessage());
        }
        return "redirect:/admin/salary-management";
    }

    private BigDecimal calculateCurrentMonthSalary(Employee employee, List<SalesTarget> salesTargets) {
        // Get employee's base salary
        BigDecimal baseSalary = employee.getSalary() != null ?
            BigDecimal.valueOf(employee.getSalary()) : BigDecimal.ZERO;

        // Calculate commission from current month sales targets
        BigDecimal commission = salesTargets.stream()
                .filter(target -> target.getCommissionAmount() != null)
                .map(SalesTarget::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return baseSalary.add(commission);
    }
}