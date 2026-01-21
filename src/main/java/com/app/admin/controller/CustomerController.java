package com.app.admin.controller;

import com.app.model.Customer;
import com.app.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/customers")
public class CustomerController {
    
    @Autowired
    private CustomerService customerService;
    
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
    public String showCustomerForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "admin/customers/form";
    }
    
    @GetMapping("/edit/{id}")
    public String editCustomer(@PathVariable Long id, Model model) {
        Customer customer = customerService.getCustomerById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer ID: " + id));
        model.addAttribute("customer", customer);
        return "admin/customers/form";
    }
    
    @PostMapping("/save")
    public String saveCustomer(@Valid @ModelAttribute Customer customer,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/customers/form";
        }
        
        customerService.saveCustomer(customer);
        redirectAttributes.addFlashAttribute("message", "Customer saved successfully!");
        return "redirect:/customers";
    }
    
    @GetMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        customerService.deleteCustomer(id);
        redirectAttributes.addFlashAttribute("message", "Customer deleted successfully!");
        return "redirect:/customers";
    }
    
    @GetMapping("/view/{id}")
    public String viewCustomer(@PathVariable Long id, Model model) {
        Customer customer = customerService.getCustomerById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer ID: " + id));
        model.addAttribute("customer", customer);
        return "admin/customers/view";
    }
}
