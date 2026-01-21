package com.app.employee.controller;

import com.app.model.LeaveRequest;
import com.app.model.Employee;
import com.app.service.LeaveRequestService;
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
@RequestMapping("/employee/leave-requests")
public class LeaveRequestController {
    
    @Autowired
    private LeaveRequestService leaveRequestService;
    
    @Autowired
    private EmployeeService employeeService;
    
    private Employee getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return employeeService.getEmployeeByUsername(username)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }
    
    @GetMapping
    public String listLeaveRequests(Model model) {
        Employee employee = getCurrentEmployee();
        List<LeaveRequest> leaveRequests = leaveRequestService.getLeaveRequestsByEmployee(employee);
        model.addAttribute("employee", employee);
        model.addAttribute("leaveRequests", leaveRequests);
        return "employee/leave-requests/list";
    }
    
    @GetMapping("/new")
    public String showLeaveRequestForm(Model model) {
        Employee employee = getCurrentEmployee();
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        model.addAttribute("employee", employee);
        model.addAttribute("leaveRequest", leaveRequest);
        return "employee/leave-requests/form";
    }
    
    @GetMapping("/view/{id}")
    public String viewLeaveRequest(@PathVariable Long id, Model model) {
        Employee employee = getCurrentEmployee();
        LeaveRequest leaveRequest = leaveRequestService.getLeaveRequestById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));
        
        // Verify ownership
        if (!leaveRequest.getEmployee().getId().equals(employee.getId())) {
            throw new RuntimeException("Unauthorized access");
        }
        
        model.addAttribute("employee", employee);
        model.addAttribute("leaveRequest", leaveRequest);
        return "employee/leave-requests/view";
    }
    
    @PostMapping("/save")
    public String saveLeaveRequest(@ModelAttribute LeaveRequest leaveRequest,
                                  RedirectAttributes redirectAttributes) {
        try {
            Employee employee = getCurrentEmployee();
            leaveRequest.setEmployee(employee);
            leaveRequest.setStatus("PENDING");
            leaveRequestService.saveLeaveRequest(leaveRequest);
            redirectAttributes.addFlashAttribute("message", "Leave request submitted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employee/leave-requests";
    }
    
    @GetMapping("/cancel/{id}")
    public String cancelLeaveRequest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Employee employee = getCurrentEmployee();
            LeaveRequest leaveRequest = leaveRequestService.getLeaveRequestById(id)
                    .orElseThrow(() -> new RuntimeException("Leave request not found"));
            
            // Verify ownership
            if (!leaveRequest.getEmployee().getId().equals(employee.getId())) {
                throw new RuntimeException("Unauthorized access");
            }
            
            leaveRequestService.cancelLeaveRequest(id);
            redirectAttributes.addFlashAttribute("message", "Leave request cancelled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employee/leave-requests";
    }
}
