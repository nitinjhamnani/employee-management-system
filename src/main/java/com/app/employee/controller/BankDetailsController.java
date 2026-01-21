package com.app.employee.controller;

import com.app.model.BankDetails;
import com.app.model.Employee;
import com.app.service.BankDetailsService;
import com.app.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/employee/bank-details")
public class BankDetailsController {
    
    @Autowired
    private BankDetailsService bankDetailsService;
    
    @Autowired
    private EmployeeService employeeService;
    
    private Employee getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return employeeService.getEmployeeByUsername(username)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }
    
    @GetMapping
    public String listBankDetails(Model model) {
        Employee employee = getCurrentEmployee();
        List<BankDetails> bankDetailsList = bankDetailsService.getBankDetailsByEmployee(employee);
        model.addAttribute("employee", employee);
        model.addAttribute("bankDetailsList", bankDetailsList);
        return "employee/bank-details/list";
    }
    
    @GetMapping("/new")
    public String showBankDetailsForm(Model model) {
        Employee employee = getCurrentEmployee();
        BankDetails bankDetails = new BankDetails();
        bankDetails.setEmployee(employee);
        model.addAttribute("employee", employee);
        model.addAttribute("bankDetails", bankDetails);
        return "employee/bank-details/form";
    }
    
    @GetMapping("/edit/{id}")
    public String editBankDetails(@PathVariable Long id, Model model) {
        Employee employee = getCurrentEmployee();
        BankDetails bankDetails = bankDetailsService.getBankDetailsById(id)
                .orElseThrow(() -> new RuntimeException("Bank details not found"));
        
        // Verify ownership
        if (!bankDetails.getEmployee().getId().equals(employee.getId())) {
            throw new RuntimeException("Unauthorized access");
        }
        
        model.addAttribute("employee", employee);
        model.addAttribute("bankDetails", bankDetails);
        return "employee/bank-details/form";
    }
    
    @PostMapping("/save")
    public String saveBankDetails(@ModelAttribute BankDetails bankDetails,
                                  RedirectAttributes redirectAttributes) {
        try {
            Employee employee = getCurrentEmployee();
            bankDetails.setEmployee(employee);
            bankDetailsService.saveBankDetails(bankDetails);
            redirectAttributes.addFlashAttribute("message", "Bank details saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employee/bank-details";
    }
    
    @GetMapping("/delete/{id}")
    public String deleteBankDetails(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Employee employee = getCurrentEmployee();
            BankDetails bankDetails = bankDetailsService.getBankDetailsById(id)
                    .orElseThrow(() -> new RuntimeException("Bank details not found"));
            
            // Verify ownership
            if (!bankDetails.getEmployee().getId().equals(employee.getId())) {
                throw new RuntimeException("Unauthorized access");
            }
            
            bankDetailsService.deleteBankDetails(id);
            redirectAttributes.addFlashAttribute("message", "Bank details deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employee/bank-details";
    }
}
