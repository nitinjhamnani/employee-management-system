package com.app.admin.controller;

import com.app.model.Customer;
import com.app.model.Employee;
import com.app.service.CustomerService;
import com.app.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/customers")
public class CustomerController {
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private EmployeeService employeeService;
    
    @GetMapping
    public String listCustomers(Model model, @RequestParam(required = false) String search) {
        List<Customer> customers;
        if (search != null && !search.isEmpty()) {
            customers = customerService.searchCustomers(search);
        } else {
            customers = customerService.getAllCustomers();
        }
        model.addAttribute("customers", customers);
        model.addAttribute("search", search);
        return "admin/customers/list";
    }
    
    @GetMapping("/new")
    public String showCustomerForm(Model model, @RequestParam(required = false) Long assignToEmployeeId) {
        Customer customer = new Customer();
        if (assignToEmployeeId != null) {
            Employee employee = employeeService.getEmployeeById(assignToEmployeeId).orElse(null);
            if (employee != null) {
                // Pre-populate hierarchy fields based on employee
                customerService.saveCustomer(customer, employee);
            }
        }
        model.addAttribute("customer", customer);
        model.addAttribute("employees", employeeService.getAllEmployees());
        return "admin/customers/form";
    }
    
    @GetMapping("/edit/{id}")
    public String editCustomer(@PathVariable Long id, Model model) {
        Customer customer = customerService.getCustomerById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer ID: " + id));
        model.addAttribute("customer", customer);
        model.addAttribute("employees", employeeService.getAllEmployees());
        return "admin/customers/form";
    }
    
    @PostMapping("/save")
    public String saveCustomer(@Valid @ModelAttribute Customer customer,
                              @RequestParam(required = false) Long assignToEmployeeId,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("employees", employeeService.getAllEmployees());
            return "admin/customers/form";
        }
        
        // Check for duplicate email+phone combination
        if (customer.getId() == null) {
            if (customerService.emailAndPhoneExists(customer.getEmail(), customer.getPhone())) {
                result.rejectValue("email", "error.customer", "A customer with this email and phone number combination already exists");
                result.rejectValue("phone", "error.customer", "A customer with this phone number combination already exists");
                model.addAttribute("employees", employeeService.getAllEmployees());
                return "admin/customers/form";
            }
        } else {
            if (customerService.emailAndPhoneExistsForOtherCustomer(customer.getEmail(), customer.getPhone(), customer.getId())) {
                result.rejectValue("email", "error.customer", "A customer with this email and phone number combination already exists");
                result.rejectValue("phone", "error.customer", "A customer with this phone number combination already exists");
                model.addAttribute("employees", employeeService.getAllEmployees());
                return "admin/customers/form";
            }
        }
        
        // If assigning to an employee, populate hierarchy fields
        if (assignToEmployeeId != null) {
            Employee employee = employeeService.getEmployeeById(assignToEmployeeId).orElse(null);
            if (employee != null && customer.getId() == null) {
                // Only populate for new customers
                customerService.saveCustomer(customer, employee);
            } else {
                customerService.saveCustomer(customer);
            }
        } else {
            customerService.saveCustomer(customer);
        }
        
        redirectAttributes.addFlashAttribute("message", "Customer saved successfully!");
        return "redirect:/admin/customers";
    }
    
    @GetMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        customerService.deleteCustomer(id);
        redirectAttributes.addFlashAttribute("message", "Customer deleted successfully!");
        return "redirect:/admin/customers";
    }
    
    @GetMapping("/view/{id}")
    public String viewCustomer(@PathVariable Long id, Model model) {
        Customer customer = customerService.getCustomerById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer ID: " + id));
        model.addAttribute("customer", customer);
        return "admin/customers/view";
    }
}
