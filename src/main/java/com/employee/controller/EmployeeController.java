package com.employee.controller;

import com.employee.model.Employee;
import com.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/employees")
public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;
    
    @GetMapping
    public String listEmployees(Model model, @RequestParam(required = false) String search) {
        List<Employee> employees;
        if (search != null && !search.isEmpty()) {
            employees = employeeService.searchEmployees(search);
        } else {
            employees = employeeService.getAllEmployees();
        }
        model.addAttribute("employees", employees);
        model.addAttribute("search", search);
        return "employees/list";
    }
    
    @GetMapping("/new")
    public String showEmployeeForm(Model model) {
        model.addAttribute("employee", new Employee());
        // Get all active employees as potential reporting managers
        List<Employee> managers = employeeService.getActiveEmployees();
        model.addAttribute("managers", managers);
        return "employees/form";
    }
    
    @GetMapping("/edit/{id}")
    public String editEmployee(@PathVariable Long id, Model model) {
        Employee employee = employeeService.getEmployeeById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee ID: " + id));
        model.addAttribute("employee", employee);
        // Get all active employees as potential reporting managers (excluding self)
        List<Employee> managers = employeeService.getActiveEmployees().stream()
                .filter(e -> !e.getId().equals(id))
                .toList();
        model.addAttribute("managers", managers);
        return "employees/form";
    }
    
    @PostMapping("/save")
    public String saveEmployee(@Valid @ModelAttribute Employee employee, 
                              @RequestParam(required = false) Long reportingManagerId,
                              BindingResult result, 
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            List<Employee> managers = employeeService.getActiveEmployees().stream()
                    .filter(e -> employee.getId() == null || !e.getId().equals(employee.getId()))
                    .toList();
            model.addAttribute("managers", managers);
            return "employees/form";
        }
        
        // Check for duplicate email
        if (employee.getId() == null) {
            if (employeeService.emailExists(employee.getEmail())) {
                result.rejectValue("email", "error.employee", "Email already exists");
                List<Employee> managers = employeeService.getActiveEmployees();
                model.addAttribute("managers", managers);
                return "employees/form";
            }
        } else {
            if (employeeService.emailExistsForOtherEmployee(employee.getEmail(), employee.getId())) {
                result.rejectValue("email", "error.employee", "Email already exists");
                List<Employee> managers = employeeService.getActiveEmployees().stream()
                        .filter(e -> !e.getId().equals(employee.getId()))
                        .toList();
                model.addAttribute("managers", managers);
                return "employees/form";
            }
        }
        
        // Set reporting manager if provided
        if (reportingManagerId != null) {
            Employee reportingManager = employeeService.getEmployeeById(reportingManagerId)
                    .orElse(null);
            employee.setReportingManager(reportingManager);
        } else {
            employee.setReportingManager(null);
        }
        
        employeeService.saveEmployee(employee);
        redirectAttributes.addFlashAttribute("message", "Employee saved successfully!");
        return "redirect:/employees";
    }
    
    @GetMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        employeeService.deleteEmployee(id);
        redirectAttributes.addFlashAttribute("message", "Employee deleted successfully!");
        return "redirect:/employees";
    }
    
    @GetMapping("/view/{id}")
    public String viewEmployee(@PathVariable Long id, Model model) {
        Employee employee = employeeService.getEmployeeById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee ID: " + id));
        model.addAttribute("employee", employee);
        return "employees/view";
    }
}

