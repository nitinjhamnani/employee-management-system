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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
    
    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        List<Employee> employees = employeeService.getAllEmployees();
        List<Attendance> todayAttendances = attendanceService.getAttendancesByDate(LocalDate.now());
        List<Customer> customers = customerService.getAllCustomers();
        List<Sale> recentSales = saleService.getSalesByDateRange(LocalDate.now().minusDays(30), LocalDate.now());
        
        long activeEmployees = employees.stream().filter(e -> "ACTIVE".equals(e.getStatus())).count();
        long presentToday = todayAttendances.stream().filter(a -> "PRESENT".equals(a.getStatus())).count();
        long activeCustomers = customers.stream().filter(c -> "ACTIVE".equals(c.getStatus())).count();
        
        // Calculate revenue by product for different periods
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate startOfQuarter = today.withMonth(((today.getMonthValue() - 1) / 3) * 3 + 1).withDayOfMonth(1);
        
        Map<String, BigDecimal> dailyRevenue = saleService.getRevenueByProduct(today, today);
        Map<String, BigDecimal> monthlyRevenue = saleService.getRevenueByProduct(startOfMonth, today);
        Map<String, BigDecimal> quarterlyRevenue = saleService.getRevenueByProduct(startOfQuarter, today);
        
        // Calculate totals
        BigDecimal dailyTotal = dailyRevenue.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal monthlyTotal = monthlyRevenue.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal quarterlyTotal = quarterlyRevenue.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        
        model.addAttribute("totalEmployees", employees.size());
        model.addAttribute("activeEmployees", activeEmployees);
        model.addAttribute("presentToday", presentToday);
        model.addAttribute("totalCustomers", customers.size());
        model.addAttribute("activeCustomers", activeCustomers);
        model.addAttribute("recentSales", recentSales);
        model.addAttribute("dailyRevenue", dailyRevenue);
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("quarterlyRevenue", quarterlyRevenue);
        model.addAttribute("dailyTotal", dailyTotal);
        model.addAttribute("monthlyTotal", monthlyTotal);
        model.addAttribute("quarterlyTotal", quarterlyTotal);
        
        return "admin/index";
    }
}
