package com.app.employee.controller;

import com.app.model.Claim;
import com.app.model.Employee;
import com.app.service.ClaimService;
import com.app.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/employee/claims")
public class ClaimController {
    
    @Autowired
    private ClaimService claimService;
    
    @Autowired
    private EmployeeService employeeService;
    
    private Employee getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return employeeService.getEmployeeByUsername(username)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }
    
    @GetMapping
    public String listClaims(Model model) {
        Employee employee = getCurrentEmployee();
        List<Claim> claims = claimService.getClaimsByEmployee(employee);
        model.addAttribute("employee", employee);
        model.addAttribute("claims", claims);
        return "employee/claims/list";
    }
    
    @GetMapping("/new")
    public String showClaimForm(Model model) {
        Employee employee = getCurrentEmployee();
        Claim claim = new Claim();
        claim.setEmployee(employee);
        model.addAttribute("employee", employee);
        model.addAttribute("claim", claim);
        return "employee/claims/form";
    }
    
    @GetMapping("/view/{id}")
    public String viewClaim(@PathVariable Long id, Model model) {
        Employee employee = getCurrentEmployee();
        Claim claim = claimService.getClaimById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        
        // Verify ownership
        if (!claim.getEmployee().getId().equals(employee.getId())) {
            throw new RuntimeException("Unauthorized access");
        }
        
        model.addAttribute("employee", employee);
        model.addAttribute("claim", claim);
        return "employee/claims/view";
    }
    
    @PostMapping("/save")
    public String saveClaim(@ModelAttribute Claim claim,
                           RedirectAttributes redirectAttributes) {
        try {
            Employee employee = getCurrentEmployee();
            claim.setEmployee(employee);
            claim.setStatus("PENDING");
            claimService.saveClaim(claim);
            redirectAttributes.addFlashAttribute("message", "Claim submitted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employee/claims";
    }
    
    @GetMapping("/delete/{id}")
    public String deleteClaim(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Employee employee = getCurrentEmployee();
            Claim claim = claimService.getClaimById(id)
                    .orElseThrow(() -> new RuntimeException("Claim not found"));
            
            // Verify ownership and allow deletion only if pending
            if (!claim.getEmployee().getId().equals(employee.getId())) {
                throw new RuntimeException("Unauthorized access");
            }
            if (!claim.getStatus().equals("PENDING")) {
                throw new RuntimeException("Cannot delete a claim that has been processed");
            }
            
            claimService.deleteClaim(id);
            redirectAttributes.addFlashAttribute("message", "Claim deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employee/claims";
    }
}
