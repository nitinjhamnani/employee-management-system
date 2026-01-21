package com.app.employee.controller;

import com.app.model.Employee;
import com.app.model.Attendance;
import com.app.model.Task;
import com.app.model.SalesTarget;
import com.app.model.Sale;
import com.app.model.Customer;
import com.app.service.EmployeeService;
import com.app.service.AttendanceService;
import com.app.service.TaskService;
import com.app.service.SalesTargetService;
import com.app.service.SaleService;
import com.app.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/employee")
public class EmployeeDashboardController {
    
    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private AttendanceService attendanceService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private SalesTargetService salesTargetService;
    
    @Autowired
    private SaleService saleService;
    
    @Autowired
    private CustomerService customerService;
    
    private Employee getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Employee employee = employeeService.getEmployeeByUsername(username)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return employee;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Employee employee = getCurrentEmployee();
        
        // Get today's attendance
        Attendance todayAttendance = attendanceService.getTodayAttendance(employee.getId());
        
        // Get tasks
        List<Task> tasks = taskService.getTasksByEmployee(employee);
        List<Task> openTasks = taskService.getTasksByEmployeeAndStatus(employee, "OPEN");
        List<Task> inProgressTasks = taskService.getTasksByEmployeeAndStatus(employee, "IN_PROGRESS");
        
        // Get current sales target
        Optional<SalesTarget> currentTarget = salesTargetService.getCurrentSalesTarget(employee);
        
        // Get recent sales
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        List<Sale> recentSales = saleService.getSalesByEmployeeAndDateRange(employee.getId(), startDate, endDate);
        
        model.addAttribute("employee", employee);
        model.addAttribute("todayAttendance", todayAttendance);
        model.addAttribute("tasks", tasks);
        model.addAttribute("openTasks", openTasks);
        model.addAttribute("inProgressTasks", inProgressTasks);
        model.addAttribute("currentTarget", currentTarget.orElse(null));
        model.addAttribute("recentSales", recentSales);
        
        return "employee/dashboard";
    }
    
    @PostMapping("/attendance/checkin")
    public String checkIn(RedirectAttributes redirectAttributes) {
        try {
            Employee employee = getCurrentEmployee();
            attendanceService.checkIn(employee.getId());
            redirectAttributes.addFlashAttribute("message", "Checked in successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employee/dashboard";
    }
    
    @PostMapping("/attendance/checkout")
    public String checkOut(RedirectAttributes redirectAttributes) {
        try {
            Employee employee = getCurrentEmployee();
            attendanceService.checkOut(employee.getId());
            redirectAttributes.addFlashAttribute("message", "Checked out successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employee/dashboard";
    }
    
    @GetMapping("/tasks")
    public String tasks(Model model) {
        Employee employee = getCurrentEmployee();
        List<Task> tasks = taskService.getTasksByEmployee(employee);
        model.addAttribute("tasks", tasks);
        model.addAttribute("employee", employee);
        return "employee/tasks";
    }
    
    @PostMapping("/tasks/{id}/update-status")
    public String updateTaskStatus(@PathVariable Long id, 
                                   @RequestParam String status,
                                   RedirectAttributes redirectAttributes) {
        try {
            taskService.updateTaskStatus(id, status);
            redirectAttributes.addFlashAttribute("message", "Task status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employee/tasks";
    }
    
    @GetMapping("/sales-target")
    public String salesTarget(Model model) {
        Employee employee = getCurrentEmployee();
        List<SalesTarget> targets = salesTargetService.getSalesTargetsByEmployee(employee);
        Optional<SalesTarget> currentTarget = salesTargetService.getCurrentSalesTarget(employee);
        
        model.addAttribute("employee", employee);
        model.addAttribute("targets", targets);
        model.addAttribute("currentTarget", currentTarget.orElse(null));
        
        return "employee/sales-target";
    }
    
    @GetMapping("/customers/new")
    public String showCustomerForm(Model model) {
        Employee employee = getCurrentEmployee();
        model.addAttribute("employee", employee);
        model.addAttribute("customer", new Customer());
        return "employee/customer-form";
    }
    
    @PostMapping("/customers/save")
    public String saveCustomer(@ModelAttribute Customer customer,
                              RedirectAttributes redirectAttributes) {
        try {
            customerService.saveCustomer(customer);
            redirectAttributes.addFlashAttribute("message", "Customer created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employee/dashboard";
    }
    
    @GetMapping("/profile")
    public String showProfile(Model model) {
        Employee employee = getCurrentEmployee();
        model.addAttribute("employee", employee);
        return "employee/profile";
    }
    
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute Employee employeeUpdate,
                                RedirectAttributes redirectAttributes) {
        try {
            Employee currentEmployee = getCurrentEmployee();
            
            // Update only allowed fields (name, email, phone, address fields)
            currentEmployee.setFirstName(employeeUpdate.getFirstName());
            currentEmployee.setLastName(employeeUpdate.getLastName());
            currentEmployee.setEmail(employeeUpdate.getEmail());
            currentEmployee.setPhone(employeeUpdate.getPhone());
            currentEmployee.setState(employeeUpdate.getState());
            currentEmployee.setCity(employeeUpdate.getCity());
            currentEmployee.setArea(employeeUpdate.getArea());
            
            employeeService.saveEmployee(currentEmployee);
            redirectAttributes.addFlashAttribute("message", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employee/profile";
    }
}
