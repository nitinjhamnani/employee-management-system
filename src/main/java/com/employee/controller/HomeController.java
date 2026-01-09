package com.employee.controller;

import com.employee.model.Employee;
import com.employee.model.Attendance;
import com.employee.model.Customer;
import com.employee.model.Sale;
import com.employee.service.EmployeeService;
import com.employee.service.AttendanceService;
import com.employee.service.CustomerService;
import com.employee.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class HomeController {
    
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
        
        return "index";
    }
}

