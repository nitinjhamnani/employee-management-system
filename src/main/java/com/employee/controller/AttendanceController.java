package com.employee.controller;

import com.employee.model.Attendance;
import com.employee.model.Employee;
import com.employee.service.AttendanceService;
import com.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/attendances")
public class AttendanceController {
    
    @Autowired
    private AttendanceService attendanceService;
    
    @Autowired
    private EmployeeService employeeService;
    
    @GetMapping
    public String listAttendances(Model model,
                                 @RequestParam(required = false) Long employeeId,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Attendance> attendances;
        
        if (employeeId != null) {
            attendances = attendanceService.getAttendancesByEmployee(employeeId);
            model.addAttribute("selectedEmployeeId", employeeId);
        } else if (date != null) {
            attendances = attendanceService.getAttendancesByDate(date);
            model.addAttribute("selectedDate", date);
        } else {
            attendances = attendanceService.getAttendancesByDate(LocalDate.now());
            model.addAttribute("selectedDate", LocalDate.now());
        }
        
        model.addAttribute("attendances", attendances);
        model.addAttribute("employees", employeeService.getActiveEmployees());
        return "attendances/list";
    }
    
    @GetMapping("/new")
    public String showAttendanceForm(Model model) {
        model.addAttribute("attendance", new Attendance());
        model.addAttribute("employees", employeeService.getActiveEmployees());
        return "attendances/form";
    }
    
    @GetMapping("/edit/{id}")
    public String editAttendance(@PathVariable Long id, Model model) {
        Attendance attendance = attendanceService.getAttendanceById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid attendance ID: " + id));
        model.addAttribute("attendance", attendance);
        model.addAttribute("employees", employeeService.getActiveEmployees());
        return "attendances/form";
    }
    
    @PostMapping("/save")
    public String saveAttendance(@Valid @ModelAttribute Attendance attendance,
                                @RequestParam(required = false) Long employeeId,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (employeeId != null) {
            Employee employee = employeeService.getEmployeeById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid employee ID: " + employeeId));
            attendance.setEmployee(employee);
        }
        
        if (result.hasErrors()) {
            model.addAttribute("employees", employeeService.getActiveEmployees());
            return "attendances/form";
        }
        
        attendanceService.saveAttendance(attendance);
        redirectAttributes.addFlashAttribute("message", "Attendance saved successfully!");
        return "redirect:/attendances";
    }
    
    @GetMapping("/delete/{id}")
    public String deleteAttendance(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        attendanceService.deleteAttendance(id);
        redirectAttributes.addFlashAttribute("message", "Attendance deleted successfully!");
        return "redirect:/attendances";
    }
    
    @PostMapping("/checkin/{employeeId}")
    public String checkIn(@PathVariable Long employeeId, RedirectAttributes redirectAttributes) {
        try {
            attendanceService.checkIn(employeeId);
            redirectAttributes.addFlashAttribute("message", "Checked in successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/attendances";
    }
    
    @PostMapping("/checkout/{employeeId}")
    public String checkOut(@PathVariable Long employeeId, RedirectAttributes redirectAttributes) {
        try {
            attendanceService.checkOut(employeeId);
            redirectAttributes.addFlashAttribute("message", "Checked out successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/attendances";
    }
}

