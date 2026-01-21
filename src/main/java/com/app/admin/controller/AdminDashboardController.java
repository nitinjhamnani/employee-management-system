package com.app.admin.controller;

import com.app.model.Employee;
import com.app.model.Attendance;
import com.app.model.Customer;
import com.app.model.Sale;
import com.app.service.EmployeeService;
import com.app.service.AttendanceService;
import com.app.service.CustomerService;
import com.app.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class AdminDashboardController {
    
    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private AttendanceService attendanceService;
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private SaleService saleService;
    
    @GetMapping("/")
    public String home(Model model) {
        List<Employee> employees = employeeService.getAllEmployees();
        List<Attendance> todayAttendances = attendanceService.getAttendancesByDate(LocalDate.now());
        List<Customer> customers = customerService.getAllCustomers();
        List<Sale> recentSales = saleService.getSalesByDateRange(LocalDate.now().minusDays(30), LocalDate.now());
        
        long activeEmployees = employees.stream().filter(e -> "ACTIVE".equals(e.getStatus())).count();
        long presentToday = todayAttendances.stream().filter(a -> "PRESENT".equals(a.getStatus())).count();
        long activeCustomers = customers.stream().filter(c -> "ACTIVE".equals(c.getStatus())).count();
        
        model.addAttribute("totalEmployees", employees.size());
        model.addAttribute("activeEmployees", activeEmployees);
        model.addAttribute("presentToday", presentToday);
        model.addAttribute("totalCustomers", customers.size());
        model.addAttribute("activeCustomers", activeCustomers);
        model.addAttribute("recentSales", recentSales);
        
        return "admin/index";
    }
}
